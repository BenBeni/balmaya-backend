package com.balmaya.repo;

import com.balmaya.domain.Payment;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends VersionedRepository<Payment, UUID> {
  @Query("select p from Payment p where p.userId = :userId and p.current = true order by p.createdAt desc")
  List<Payment> findCurrentByUser(@Param("userId") String userId);
}

