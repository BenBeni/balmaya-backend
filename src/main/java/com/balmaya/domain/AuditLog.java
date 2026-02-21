package com.balmaya.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name="audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
  @Id
  @Column(columnDefinition="uuid")
  private UUID id;

  @Column(name="actor_type", nullable=false)
  private String actorType; // USER/ADMIN/SYSTEM

  @Column(name="actor_id")
  private String actorId;

  @Column(name="action", nullable=false)
  private String action;

  @Column(name="target_type", nullable=false)
  private String targetType;

  @Column(name="target_id", nullable=false)
  private String targetId;

  @Column(name="metadata", columnDefinition="jsonb")
  private String metadata;

  @Column(name="created_at", nullable=false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}

