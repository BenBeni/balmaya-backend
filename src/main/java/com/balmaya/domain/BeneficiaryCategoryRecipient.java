package com.balmaya.domain;

import com.balmaya.domain.Enums.*;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="beneficiary_category_recipients",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_benef_category_recipient_logical_version", columnNames={"logical_id","version"})
  }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryCategoryRecipient extends VersionedEntity {

  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="beneficiary_id", nullable=false)
  private UUID beneficiaryId;

  @Enumerated(EnumType.STRING)
  @Column(name="category", nullable=false)
  private ItemCategory category;

  @Column(name="recipient_name", nullable=false)
  private String recipientName;

  @Column(name="recipient_phone_e164", nullable=false)
  private String recipientPhoneE164;

  @Column(name="recipient_email")
  private String recipientEmail;

  @Column(name="recipient_account_number")
  private String recipientAccountNumber;

  @Enumerated(EnumType.STRING)
  @Column(name="provider", nullable=false)
  private Provider provider;
}

