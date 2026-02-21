package com.balmaya.domain;

import com.balmaya.domain.Enums.Provider;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="local_payout_accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocalPayoutAccount {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name="provider", nullable=false)
  private Provider provider;

  @Column(name="country", nullable=false)
  private String country;

  @Column(name="account_name", nullable=false)
  private String accountName;

  @Column(name="account_reference", nullable=false)
  private String accountReference;

  @Column(name="status", nullable=false)
  private String status;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt;

  @Column(name="updated_at", nullable=false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void prePersist() {
    OffsetDateTime now = OffsetDateTime.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}

