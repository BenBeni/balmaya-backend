package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="payment_intents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentIntent {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="payment_id", nullable=false)
  private UUID paymentId;

  @Column(name="provider", nullable=false)
  private String provider;

  @Column(name="provider_reference", nullable=false)
  private String providerReference;

  @Column(name="provider_charge_reference")
  private String providerChargeReference;

  @Column(name="provider_refund_reference")
  private String providerRefundReference;

  @Column(name="provider_payment_method_reference")
  private String providerPaymentMethodReference;

  @Column(name="provider_customer_reference")
  private String providerCustomerReference;

  @Column(name="amount", nullable=false)
  private long amount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private IntentStatus status;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}

