package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="payment_items",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_payment_item_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentItem extends VersionedEntity {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="payment_id", nullable=false)
  private UUID paymentId;

  @Enumerated(EnumType.STRING)
  @Column(name="category", nullable=false)
  private ItemCategory category;

  @Column(name="amount", nullable=false)
  private long amount;

  @Column(name="currency", nullable=false)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name="method", nullable=false)
  private ItemMethod method;

  @Column(name="biller_code")
  private String billerCode;

  @Column(name="biller_account_ref")
  private String billerAccountRef;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private ItemStatus status;

  @Column(name="failure_code")
  private String failureCode;
}

