package com.balmaya.api.dto;

import com.balmaya.domain.Enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleItemRequest {
  @NotNull public ItemCategory category;
  @Min(1) public long amount;
  @NotBlank public String currency;
  @NotNull public ItemMethod method;
  public String billerCode;
  public String billerAccountRef;
}

