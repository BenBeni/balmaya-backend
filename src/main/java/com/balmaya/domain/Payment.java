package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="payments",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_payment_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends VersionedEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="user_id", nullable=false)
  private String userId;

  @Column(name="beneficiary_id", nullable=false)
  private UUID beneficiaryId;

  @Enumerated(EnumType.STRING)
  @Column(name="kind", nullable=false)
  private PaymentKind kind;

  @Column(name="schedule_id")
  private UUID scheduleId;

  @Column(name="execute_at", nullable=false)
  private OffsetDateTime executeAt;

  @Column(name="net_amount", nullable=false)
  private long netAmount;

  @Column(name="fee_amount", nullable=false)
  private long feeAmount;

  @Column(name="gross_amount", nullable=false)
  private long grossAmount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Column(name="fx_rate")
  private java.math.BigDecimal fxRate;

  @Column(name="note")
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private PaymentStatus status;

  @Column(name="failure_code")
  private String failureCode;
}

