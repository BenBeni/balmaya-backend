package com.balmaya.service;

import com.balmaya.domain.Beneficiary;
import com.balmaya.domain.BeneficiaryCategoryRecipient;
import com.balmaya.domain.Enums.*;
import com.balmaya.lock.LockService;
import com.balmaya.repo.BeneficiaryCategoryRecipientRepository;
import com.balmaya.repo.BeneficiaryRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BeneficiaryService {
  private final BeneficiaryRepository repo;
  private final BeneficiaryCategoryRecipientRepository categoryRecipients;
  private final LockService locks;

  public List<Beneficiary> listCurrent(String userId) {
    return repo.findCurrentByUser(userId);
  }

  public Beneficiary getCurrentOwned(UUID logicalId, String userId) {
    Beneficiary b = repo.findCurrentByLogicalId(logicalId)
      .orElseThrow(() -> new EntityNotFoundException("Beneficiary logicalId not found: " + logicalId));
    if (!b.getUserId().equals(userId)) throw new EntityNotFoundException("Beneficiary not found");
    return b;
  }

  public List<BeneficiaryCategoryRecipient> getCurrentCategoryRecipients(UUID beneficiaryId) {
    return categoryRecipients.findCurrentByBeneficiaryId(beneficiaryId);
  }

  private void ensureUniqueCategories(List<BeneficiaryCategoryRecipient> recipients) {
    EnumSet<ItemCategory> seen = EnumSet.noneOf(ItemCategory.class);
    for (BeneficiaryCategoryRecipient r : recipients) {
      if (!seen.add(r.getCategory())) {
        throw new IllegalArgumentException("Duplicate category recipient: " + r.getCategory());
      }
    }
  }

  private void saveCategoryRecipients(UUID beneficiaryId, List<BeneficiaryCategoryRecipient> recipients, String actorId, String reason) {
    for (BeneficiaryCategoryRecipient r : recipients) {
      r.setId(UUID.randomUUID());
      r.setBeneficiaryId(beneficiaryId);
      Versioning.initNew(r, actorId);
      r.setChangeReason(reason);
      categoryRecipients.save(r);
    }
  }

  @Transactional
  public Beneficiary create(String userId, String actorId, String fullName, String relationship, String phoneE164, Provider provider, String country,
                            List<BeneficiaryCategoryRecipient> newRecipients, String reason) {
    ensureUniqueCategories(newRecipients);
    Beneficiary b = Beneficiary.builder()
      .id(UUID.randomUUID())
      .userId(userId)
      .fullName(fullName)
      .relationship(relationship)
      .phoneE164(phoneE164)
      .provider(provider)
      .country(country)
      .status(BeneficiaryStatus.ACTIVE)
      .build();
    Versioning.initNew(b, actorId);
    b.setChangeReason(reason);
    Beneficiary saved = repo.save(b);
    saveCategoryRecipients(saved.getId(), newRecipients, actorId, reason);
    return saved;
  }

  @Transactional
  public Beneficiary update(UUID logicalId, String userId, String actorId, String fullName, String relationship, String phoneE164, Provider provider,
                            String country, BeneficiaryStatus status, List<BeneficiaryCategoryRecipient> newRecipients, String reason) {
    return locks.withLock("lock:beneficiary:" + logicalId, 5, 30, () -> {
      ensureUniqueCategories(newRecipients);
      Beneficiary cur = getCurrentOwned(logicalId, userId);
      if (!cur.isCurrent()) throw new IllegalStateException("Not current version");

      // retire old
      cur.setCurrent(false);
      repo.saveAndFlush(cur);

      Beneficiary next = Beneficiary.builder()
        .id(UUID.randomUUID())
        .userId(cur.getUserId())
        .fullName(fullName)
        .relationship(relationship)
        .phoneE164(phoneE164)
        .provider(provider)
        .country(country)
        .status(status)
        .build();
      Versioning.nextVersion(next, cur, actorId, reason);
      Beneficiary saved = repo.save(next);

      List<BeneficiaryCategoryRecipient> oldRecipients = categoryRecipients.findCurrentByBeneficiaryId(cur.getId());
      for (BeneficiaryCategoryRecipient r : oldRecipients) {
        r.setCurrent(false);
        categoryRecipients.saveAndFlush(r);
      }

      saveCategoryRecipients(saved.getId(), newRecipients, actorId, reason);
      return saved;
    });
  }

  public List<Beneficiary> listVersions(UUID logicalId) {
    return repo.findAllVersions(logicalId);
  }
}

