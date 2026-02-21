package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="payment_fee_components",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_fee_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentFeeComponent extends VersionedEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="payment_id", nullable=false)
  private UUID paymentId;

  @Enumerated(EnumType.STRING)
  @Column(name="code", nullable=false)
  private FeeCode code;

  @Column(name="description")
  private String description;

  @Column(name="amount", nullable=false)
  private long amount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name="direction", nullable=false)
  private FeeDirection direction;

  @Column(name="is_refundable", nullable=false)
  private boolean refundable;

  @Column(name="provider")
  private String provider;

  @Column(name="provider_reference")
  private String providerReference;
}

