package com.balmaya.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="email_otps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailOtp {

  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="email", nullable=false)
  private String email;

  @Column(name="user_id")
  private String userId;

  @Column(name="code", nullable=false)
  private String code;

  @Column(name="expires_at", nullable=false)
  private OffsetDateTime expiresAt;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt;

  @Column(name="last_sent_at")
  private OffsetDateTime lastSentAt;

  @Column(name="used_at")
  private OffsetDateTime usedAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}

