package com.balmaya.service;

import com.balmaya.domain.Beneficiary;
import com.balmaya.domain.Schedule;
import com.balmaya.domain.ScheduleItem;
import com.balmaya.domain.Enums.*;
import com.balmaya.lock.LockService;
import com.balmaya.repo.BeneficiaryRepository;
import com.balmaya.repo.ScheduleItemRepository;
import com.balmaya.repo.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {
  private final BeneficiaryRepository beneficiaries;
  private final ScheduleRepository schedules;
  private final ScheduleItemRepository items;
  private final LockService locks;

  public List<Schedule> listCurrent(String userId) {
    return schedules.findCurrentByUser(userId);
  }

  public Schedule getCurrentOwned(UUID logicalId, String userId) {
    Schedule s = schedules.findCurrentByLogicalId(logicalId)
      .orElseThrow(() -> new EntityNotFoundException("Schedule logicalId not found: " + logicalId));
    if (!s.getUserId().equals(userId)) throw new EntityNotFoundException("Schedule not found");
    return s;
  }

  private Beneficiary getCurrentBeneficiaryOwned(UUID beneficiaryLogicalId, String userId) {
    Beneficiary b = beneficiaries.findCurrentByLogicalId(beneficiaryLogicalId)
      .orElseThrow(() -> new EntityNotFoundException("Beneficiary logicalId not found: " + beneficiaryLogicalId));
    if (!b.getUserId().equals(userId)) throw new EntityNotFoundException("Beneficiary not found");
    return b;
  }

  public List<ScheduleItem> getCurrentItems(UUID scheduleId) {
    return items.findCurrentByScheduleId(scheduleId);
  }

  private OffsetDateTime computeNextRun(OffsetDateTime from, Frequency freq, Integer weeklyDay, Integer monthlyDay) {
    // Very simple: next_run_at = start_at if in future else +7d or +1m
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime base = from.isAfter(now) ? from : now;
    if (freq == Frequency.WEEKLY) return base.plusDays(7);
    return base.plusMonths(1);
  }

  private long sumItems(List<ScheduleItem> itemRows) {
    return itemRows.stream().mapToLong(ScheduleItem::getAmount).sum();
  }

  @Transactional
  public Schedule create(String userId, String actorId, UUID beneficiaryId, Frequency frequency, OffsetDateTime startAt, String timezone,
                         Integer weeklyDay, Integer monthlyDay, String currency, List<ScheduleItem> newItems, String reason) {
    getCurrentBeneficiaryOwned(beneficiaryId, userId);
    Schedule s = Schedule.builder()
      .id(UUID.randomUUID())
      .userId(userId)
      .beneficiaryId(beneficiaryId)
      .status(ScheduleStatus.ACTIVE)
      .frequency(frequency)
      .timezone(timezone)
      .startAt(startAt)
      .weeklyDay(weeklyDay)
      .monthlyDay(monthlyDay)
      .nextRunAt(computeNextRun(startAt, frequency, weeklyDay, monthlyDay))
      .currency(currency)
      .build();
    Versioning.initNew(s, actorId);
    s.setChangeReason(reason);

    Schedule saved = schedules.save(s);

    for (ScheduleItem si : newItems) {
      si.setId(UUID.randomUUID());
      si.setScheduleId(saved.getId());
      Versioning.initNew(si, actorId);
      si.setChangeReason(reason);
      items.save(si);
    }
    // store net amount for UI
    saved.setNetAmount(sumItems(items.findCurrentByScheduleId(saved.getId())));
    return schedules.save(saved);
  }

  @Transactional
  public Schedule update(UUID logicalId, String userId, String actorId, ScheduleStatus status, Frequency frequency, OffsetDateTime startAt,
                         String timezone, Integer weeklyDay, Integer monthlyDay, String currency, List<ScheduleItem> newItems, String reason) {
    return locks.withLock("lock:schedule:" + logicalId, 5, 30, () -> {
      Schedule cur = getCurrentOwned(logicalId, userId);
      cur.setCurrent(false);
      schedules.saveAndFlush(cur);

      Schedule next = Schedule.builder()
        .id(UUID.randomUUID())
        .userId(cur.getUserId())
        .beneficiaryId(cur.getBeneficiaryId())
        .status(status)
        .frequency(frequency)
        .timezone(timezone)
        .startAt(startAt)
        .weeklyDay(weeklyDay)
        .monthlyDay(monthlyDay)
        .nextRunAt(computeNextRun(startAt, frequency, weeklyDay, monthlyDay))
        .currency(currency)
        .netAmount(0L)
        .build();
      Versioning.nextVersion(next, cur, actorId, reason);
      Schedule saved = schedules.save(next);

      // retire old schedule items (by schedule_id) and create new ones attached to new schedule version id
      List<ScheduleItem> oldItems = items.findCurrentByScheduleId(cur.getId());
      for (ScheduleItem oi : oldItems) { oi.setCurrent(false); items.saveAndFlush(oi); }

      for (ScheduleItem si : newItems) {
        si.setId(UUID.randomUUID());
        si.setScheduleId(saved.getId());
        Versioning.initNew(si, actorId);
        si.setChangeReason(reason);
        items.save(si);
      }
      saved.setNetAmount(sumItems(items.findCurrentByScheduleId(saved.getId())));
      return schedules.save(saved);
    });
  }

  public List<Schedule> listVersions(UUID logicalId) {
    return schedules.findAllVersions(logicalId);
  }
}

