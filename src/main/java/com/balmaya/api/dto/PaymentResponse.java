package com.balmaya.api.dto;

import com.balmaya.domain.Enums.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
  public UUID id;
  public UUID logicalId;
  public int version;
  public boolean isCurrent;

  public UUID beneficiaryId;
  public PaymentKind kind;
  public UUID scheduleId;
  public OffsetDateTime executeAt;

  public long netAmount;
  public long feeAmount;
  public long grossAmount;
  public String currency;

  public PaymentStatus status;
  public String note;

  public List<PaymentItemView> items;
  public List<FeeComponentView> fees;

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class PaymentItemView {
    public UUID id;
    public ItemCategory category;
    public long amount;
    public String currency;
    public ItemMethod method;
    public ItemStatus status;
  }

  @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
  public static class FeeComponentView {
    public UUID id;
    public FeeCode code;
    public long amount;
    public String currency;
    public FeeDirection direction;
  }
}

