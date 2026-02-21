package com.balmaya.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class VersionedEntity {

  @Column(name = "logical_id", nullable = false)
  private UUID logicalId;

  @Column(name = "version", nullable = false)
  private int version;

  @Column(name = "is_current", nullable = false)
  private boolean current;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "change_reason")
  private String changeReason;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
  }
}

