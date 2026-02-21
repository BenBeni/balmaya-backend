package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="executions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Execution {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="payment_item_id", nullable=false)
  private UUID paymentItemId;

  @Column(name="provider", nullable=false)
  private String provider;

  @Column(name="provider_reference")
  private String providerReference;

  @Column(name="destination", nullable=false)
  private String destination;

  @Column(name="amount", nullable=false)
  private long amount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private ExecutionStatus status;

  @Column(name="failure_code")
  private String failureCode;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}

