package com.balmaya.api.dto;

import com.balmaya.domain.Enums.*;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleUpdateRequest {
  @NotNull public ScheduleStatus status;
  @NotNull public Frequency frequency;
  @NotNull public OffsetDateTime startAt;
  @NotBlank public String timezone;
  public Integer weeklyDay;
  public Integer monthlyDay;

  @NotBlank public String currency;
  @NotBlank public String changeReason;

  @Size(min=1) @NotNull
  public List<ScheduleItemRequest> items;
}

