package com.balmaya.service;

import com.balmaya.domain.VersionedEntity;
import java.util.UUID;

public final class Versioning {
  private Versioning() {}

  public static void initNew(VersionedEntity e, String actorId) {
    e.setLogicalId(UUID.randomUUID());
    e.setVersion(1);
    e.setCurrent(true);
    e.setCreatedBy(actorId);
  }

  public static void nextVersion(VersionedEntity newRow, VersionedEntity currentRow, String actorId, String changeReason) {
    newRow.setLogicalId(currentRow.getLogicalId());
    newRow.setVersion(currentRow.getVersion() + 1);
    newRow.setCurrent(true);
    newRow.setCreatedBy(actorId);
    newRow.setChangeReason(changeReason);
  }
}

