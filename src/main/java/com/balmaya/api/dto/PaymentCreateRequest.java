package com.balmaya.api.dto;

import com.balmaya.domain.Enums.PaymentKind;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentCreateRequest {
  @NotNull public UUID beneficiaryId;
  @NotNull public PaymentKind kind;
  public UUID scheduleId; // for scheduled run generation (system)
  @NotNull public OffsetDateTime executeAt;
  @NotBlank public String currency; // charge currency
  public String note;
  @NotBlank public String changeReason;

  @Size(min=1) @NotNull
  public List<PaymentItemRequest> items;
}

