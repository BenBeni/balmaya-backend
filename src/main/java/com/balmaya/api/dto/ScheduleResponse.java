package com.balmaya.api.dto;

import com.balmaya.domain.Enums.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleResponse {
  public UUID id;
  public UUID logicalId;
  public int version;
  public boolean isCurrent;

  public UUID beneficiaryId;
  public ScheduleStatus status;
  public Frequency frequency;
  public String timezone;
  public OffsetDateTime startAt;
  public Integer weeklyDay;
  public Integer monthlyDay;
  public OffsetDateTime nextRunAt;

  public long netAmount;
  public String currency;

  public List<ScheduleItemView> items;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class ScheduleItemView {
    public UUID id;
    public ItemCategory category;
    public long amount;
    public String currency;
    public ItemMethod method;
  }
}

