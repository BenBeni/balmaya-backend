package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="beneficiaries",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_benef_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Beneficiary extends VersionedEntity {

  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="user_id", nullable=false)
  private String userId;

  @Column(name="full_name", nullable=false)
  private String fullName;

  @Column(name="relationship")
  private String relationship;

  @Column(name="country", nullable=false)
  private String country;

  @Enumerated(EnumType.STRING)
  @Column(name="provider", nullable=false)
  private Provider provider;

  @Column(name="phone_e164", nullable=false)
  private String phoneE164;

  @Enumerated(EnumType.STRING)
  @Column(name="status", nullable=false)
  private BeneficiaryStatus status;
}

