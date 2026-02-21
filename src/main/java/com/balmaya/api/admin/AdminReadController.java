package com.balmaya.api.admin;

import com.balmaya.api.dto.BeneficiaryCategoryRecipientResponse;
import com.balmaya.api.dto.BeneficiaryResponse;
import com.balmaya.api.dto.ScheduleResponse;
import com.balmaya.api.dto.PaymentResponse;
import com.balmaya.domain.*;
import com.balmaya.service.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/read")
public class AdminReadController {

  private final BeneficiaryService beneficiaries;
  private final ScheduleService schedules;
  private final PaymentService payments;

  @GetMapping("/beneficiaries/{logicalId}/versions")
  public List<BeneficiaryResponse> beneficiaryVersions(@PathVariable("logicalId") UUID logicalId) {
    return beneficiaries.listVersions(logicalId).stream().map(this::toBenef).toList();
  }

  @GetMapping("/schedules/{logicalId}/versions")
  public List<ScheduleResponse> scheduleVersions(@PathVariable("logicalId") UUID logicalId) {
    return schedules.listVersions(logicalId).stream()
      .map(s -> toSched(s, schedules.getCurrentItems(s.getId()))) // for historical you'd query by schedule id; simplified
      .toList();
  }

  @GetMapping("/payments/{logicalId}/versions")
  public List<PaymentResponse> paymentVersions(@PathVariable("logicalId") UUID logicalId) {
    return payments.listVersions(logicalId).stream()
      .map(p -> toPay(p, payments.getCurrentItems(p.getId()), payments.getCurrentFees(p.getId())))
      .toList();
  }

  private BeneficiaryResponse toBenef(Beneficiary b) {
    List<BeneficiaryCategoryRecipientResponse> recipients = beneficiaries.getCurrentCategoryRecipients(b.getId()).stream()
      .map(r -> BeneficiaryCategoryRecipientResponse.builder()
        .category(r.getCategory())
        .recipientName(r.getRecipientName())
        .recipientPhoneE164(r.getRecipientPhoneE164())
        .recipientEmail(r.getRecipientEmail())
        .recipientAccountNumber(r.getRecipientAccountNumber())
        .provider(r.getProvider())
        .build()
      ).toList();
    return BeneficiaryResponse.builder()
      .id(b.getId()).logicalId(b.getLogicalId()).version(b.getVersion()).isCurrent(b.isCurrent())
      .createdAt(b.getCreatedAt()).createdBy(b.getCreatedBy()).changeReason(b.getChangeReason())
      .fullName(b.getFullName()).relationship(b.getRelationship()).country(b.getCountry())
      .provider(b.getProvider()).phoneE164(b.getPhoneE164()).status(b.getStatus())
      .categoryRecipients(recipients)
      .build();
  }

  private ScheduleResponse toSched(Schedule s, List<ScheduleItem> items) {
    return ScheduleResponse.builder()
      .id(s.getId()).logicalId(s.getLogicalId()).version(s.getVersion()).isCurrent(s.isCurrent())
      .beneficiaryId(s.getBeneficiaryId()).status(s.getStatus()).frequency(s.getFrequency())
      .timezone(s.getTimezone()).startAt(s.getStartAt()).weeklyDay(s.getWeeklyDay())
      .monthlyDay(s.getMonthlyDay()).nextRunAt(s.getNextRunAt()).netAmount(s.getNetAmount()).currency(s.getCurrency())
      .items(items.stream().map(i -> ScheduleResponse.ScheduleItemView.builder()
        .id(i.getId()).category(i.getCategory()).amount(i.getAmount()).currency(i.getCurrency()).method(i.getMethod()).build()
      ).toList())
      .build();
  }

  private PaymentResponse toPay(Payment p, List<PaymentItem> items, List<PaymentFeeComponent> fees) {
    return PaymentResponse.builder()
      .id(p.getId()).logicalId(p.getLogicalId()).version(p.getVersion()).isCurrent(p.isCurrent())
      .beneficiaryId(p.getBeneficiaryId()).kind(p.getKind()).scheduleId(p.getScheduleId()).executeAt(p.getExecuteAt())
      .netAmount(p.getNetAmount()).feeAmount(p.getFeeAmount()).grossAmount(p.getGrossAmount()).currency(p.getCurrency())
      .status(p.getStatus()).note(p.getNote())
      .items(items.stream().map(i -> PaymentResponse.PaymentItemView.builder()
        .id(i.getId()).category(i.getCategory()).amount(i.getAmount()).currency(i.getCurrency()).method(i.getMethod()).status(i.getStatus()).build()
      ).toList())
      .fees(fees.stream().map(f -> PaymentResponse.FeeComponentView.builder()
        .id(f.getId()).code(f.getCode()).amount(f.getAmount()).currency(f.getCurrency()).direction(f.getDirection()).build()
      ).toList())
      .build();
  }
}

