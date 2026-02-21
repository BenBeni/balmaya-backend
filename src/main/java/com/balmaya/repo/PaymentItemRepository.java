package com.balmaya.repo;

import com.balmaya.domain.PaymentItem;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PaymentItemRepository extends VersionedRepository<PaymentItem, UUID> {
  @Query("select pi from PaymentItem pi where pi.paymentId = :paymentId and pi.current = true")
  List<PaymentItem> findCurrentByPaymentId(@Param("paymentId") UUID paymentId);
}

