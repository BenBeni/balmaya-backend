package com.balmaya.api;

import com.balmaya.api.dto.*;
import com.balmaya.auth.CurrentUser;
import com.balmaya.domain.*;
import com.balmaya.service.ScheduleService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

  private final ScheduleService service;

  @GetMapping
  public List<ScheduleResponse> list(Authentication auth) {
    String userId = CurrentUser.userId(auth);
    return service.listCurrent(userId).stream()
      .map(s -> toResp(s, service.getCurrentItems(s.getId())))
      .toList();
  }

  @PostMapping
  public ScheduleResponse create(@Valid @RequestBody ScheduleCreateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);

    List<ScheduleItem> items = new ArrayList<>();
    for (ScheduleItemRequest ir : req.items) {
      items.add(ScheduleItem.builder()
        .category(ir.category)
        .amount(ir.amount)
        .currency(ir.currency)
        .method(ir.method)
        .billerCode(ir.billerCode)
        .billerAccountRef(ir.billerAccountRef)
        .build());
    }

    Schedule s = service.create(userId, userId, req.beneficiaryId, req.frequency, req.startAt, req.timezone, req.weeklyDay, req.monthlyDay, req.currency, items, req.changeReason);
    return toResp(s, service.getCurrentItems(s.getId()));
  }

  @PutMapping("/{logicalId}")
  public ScheduleResponse update(@PathVariable("logicalId") UUID logicalId, @Valid @RequestBody ScheduleUpdateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);

    List<ScheduleItem> items = new ArrayList<>();
    for (ScheduleItemRequest ir : req.items) {
      items.add(ScheduleItem.builder()
        .category(ir.category)
        .amount(ir.amount)
        .currency(ir.currency)
        .method(ir.method)
        .billerCode(ir.billerCode)
        .billerAccountRef(ir.billerAccountRef)
        .build());
    }

    Schedule s = service.update(logicalId, userId, userId, req.status, req.frequency, req.startAt, req.timezone, req.weeklyDay, req.monthlyDay, req.currency, items, req.changeReason);
    return toResp(s, service.getCurrentItems(s.getId()));
  }

  private ScheduleResponse toResp(Schedule s, List<ScheduleItem> items) {
    return ScheduleResponse.builder()
      .id(s.getId())
      .logicalId(s.getLogicalId())
      .version(s.getVersion())
      .isCurrent(s.isCurrent())
      .beneficiaryId(s.getBeneficiaryId())
      .status(s.getStatus())
      .frequency(s.getFrequency())
      .timezone(s.getTimezone())
      .startAt(s.getStartAt())
      .weeklyDay(s.getWeeklyDay())
      .monthlyDay(s.getMonthlyDay())
      .nextRunAt(s.getNextRunAt())
      .netAmount(s.getNetAmount())
      .currency(s.getCurrency())
      .items(items.stream().map(i -> ScheduleResponse.ScheduleItemView.builder()
        .id(i.getId())
        .category(i.getCategory())
        .amount(i.getAmount())
        .currency(i.getCurrency())
        .method(i.getMethod())
        .build()).toList())
      .build();
  }
}

