package com.balmaya.repo;

import com.balmaya.domain.BeneficiaryCategoryRecipient;
import java.util.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BeneficiaryCategoryRecipientRepository extends VersionedRepository<BeneficiaryCategoryRecipient, UUID> {
  @Query("select r from BeneficiaryCategoryRecipient r where r.beneficiaryId = :beneficiaryId and r.current = true")
  List<BeneficiaryCategoryRecipient> findCurrentByBeneficiaryId(@Param("beneficiaryId") UUID beneficiaryId);
}

