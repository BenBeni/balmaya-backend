package com.balmaya.repo;

import com.balmaya.domain.PaymentFeeComponent;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentFeeComponentRepository extends VersionedRepository<PaymentFeeComponent, UUID> {
  @Query("select f from PaymentFeeComponent f where f.paymentId = :paymentId and f.current = true")
  List<PaymentFeeComponent> findCurrentByPaymentId(@Param("paymentId") UUID paymentId);
}

