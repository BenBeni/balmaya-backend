package com.balmaya.api.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentCardResponse {
  private PaymentResponse payment;
  private String stripePaymentIntentId;
  private String stripeClientSecret;
}

