package com.balmaya.service;

import com.balmaya.domain.Enums.FeeCode;
import com.balmaya.domain.Enums.FeeDirection;
import com.balmaya.domain.PaymentFeeComponent;
import java.util.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeeCalculator {

  @Value("${balmaya.fee.service_fee_flat_minor:0}")
  private long flat;

  @Value("${balmaya.fee.service_fee_percent_bps:0}")
  private long bps;

  public List<PaymentFeeComponent> computeFeeComponents(UUID paymentId, String currency, long netAmountMinor, String actorId) {
    // Simple policy: service fee = flat + net*bps/10_000
    long pct = (netAmountMinor * bps) / 10_000L;
    long serviceFee = Math.max(0, flat/100 + pct);

    PaymentFeeComponent comp = PaymentFeeComponent.builder()
      .id(UUID.randomUUID())
      .paymentId(paymentId)
      .code(FeeCode.SERVICE_FEE)
      .description("Service fee")
      .amount(serviceFee)
      .currency(currency)
      .direction(FeeDirection.CHARGE)
      .refundable(false)
      .build();
    Versioning.initNew(comp, actorId);
    return List.of(comp);
  }
}

