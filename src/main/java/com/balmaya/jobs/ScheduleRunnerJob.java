package com.balmaya.jobs;

import com.balmaya.domain.*;
import com.balmaya.domain.Enums.*;
import com.balmaya.lock.LockService;
import com.balmaya.repo.ScheduleItemRepository;
import com.balmaya.repo.ScheduleRepository;
import com.balmaya.service.PaymentService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduleRunnerJob {

  private final ScheduleRepository schedules;
  private final ScheduleItemRepository scheduleItems;
  private final PaymentService payments;
  private final LockService locks;

  @Scheduled(fixedDelayString = "${balmaya.schedule.poll_interval_ms:60000}")
  public void runDueSchedules() {
    OffsetDateTime now = OffsetDateTime.now();
    List<Schedule> due = schedules.findDueSchedules(now);
    for (Schedule s : due) {
      // lock per schedule logical id + time bucket
      String lockKey = "lock:schedule-run:" + s.getLogicalId() + ":" + s.getNextRunAt().toEpochSecond();
      locks.withLock(lockKey, 1, 55, () -> {
        // Double-check still current and due (best-effort)
        if (!s.isCurrent() || s.getStatus() != ScheduleStatus.ACTIVE || s.getNextRunAt().isAfter(OffsetDateTime.now())) return null;

        List<ScheduleItem> items = scheduleItems.findCurrentByScheduleId(s.getId());
        List<PaymentItem> paymentItems = new ArrayList<>();
        for (ScheduleItem si : items) {
          paymentItems.add(PaymentItem.builder()
            .category(si.getCategory())
            .amount(si.getAmount())
            .currency(si.getCurrency())
            .method(si.getMethod())
            .billerCode(si.getBillerCode())
            .billerAccountRef(si.getBillerAccountRef())
            .status(ItemStatus.CREATED)
            .build());
        }

        // System actor id
        String actorId = "SYSTEM";
        payments.createOneShot(
          s.getUserId(),
          actorId,
          s.getBeneficiaryId(),
          PaymentKind.SCHEDULED_RUN,
          s.getId(),
          OffsetDateTime.now(),
          s.getCurrency(),
          "Scheduled run",
          paymentItems,
          "Scheduled run"
        );

        // Advance next_run_at (very simple)
        OffsetDateTime next = s.getFrequency() == Frequency.WEEKLY ? s.getNextRunAt().plusDays(7) : s.getNextRunAt().plusMonths(1);

        // Create a new schedule version to update next_run_at (versioned updates)
        // For MVP, we update schedule by version bump with identical fields except nextRunAt.
        // NOTE: In production you might separate operational fields; here we adhere to immutable versioning.
        Schedule cur = schedules.findCurrentByLogicalId(s.getLogicalId()).orElse(null);
        if (cur == null) return null;

        cur.setCurrent(false);
        schedules.save(cur);

        Schedule nextV = Schedule.builder()
          .id(UUID.randomUUID())
          .userId(cur.getUserId())
          .beneficiaryId(cur.getBeneficiaryId())
          .status(cur.getStatus())
          .frequency(cur.getFrequency())
          .timezone(cur.getTimezone())
          .startAt(cur.getStartAt())
          .weeklyDay(cur.getWeeklyDay())
          .monthlyDay(cur.getMonthlyDay())
          .nextRunAt(next)
          .netAmount(cur.getNetAmount())
          .currency(cur.getCurrency())
          .fundingPaymentMethodRef(cur.getFundingPaymentMethodRef())
          .build();
        com.balmaya.service.Versioning.nextVersion(nextV, cur, actorId, "Advance next_run_at");
        schedules.save(nextV);

        // Copy schedule items to new schedule id (keep same logical_ids? For simplicity, we create new versions tied to new schedule id.)
        List<ScheduleItem> curItems = scheduleItems.findCurrentByScheduleId(cur.getId());
        for (ScheduleItem old : curItems) {
          old.setCurrent(false);
          scheduleItems.save(old);

          ScheduleItem newItem = ScheduleItem.builder()
            .id(UUID.randomUUID())
            .scheduleId(nextV.getId())
            .category(old.getCategory())
            .amount(old.getAmount())
            .currency(old.getCurrency())
            .method(old.getMethod())
            .billerCode(old.getBillerCode())
            .billerAccountRef(old.getBillerAccountRef())
            .build();
          com.balmaya.service.Versioning.nextVersion(newItem, old, actorId, "Carry forward schedule item");
          scheduleItems.save(newItem);
        }

        return null;
      });
    }
  }
}

