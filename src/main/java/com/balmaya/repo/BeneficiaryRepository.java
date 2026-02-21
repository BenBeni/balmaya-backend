package com.balmaya.repo;

import com.balmaya.domain.Beneficiary;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface BeneficiaryRepository extends VersionedRepository<Beneficiary, UUID> {
  @Query("select b from Beneficiary b where b.userId = :userId and b.current = true order by b.createdAt desc")
  List<Beneficiary> findCurrentByUser(@Param("userId") String userId);
}

