package com.balmaya.service;

import com.balmaya.domain.Payment;
import com.balmaya.domain.Enums.IntentStatus;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

  private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

  @Value("${balmaya.stripe.enabled:false}")
  private boolean enabled;

  @Value("${balmaya.stripe.api_key:}")
  private String apiKey;

  @PostConstruct
  void init() {
    if (enabled && apiKey != null && !apiKey.isBlank()) {
      Stripe.apiKey = apiKey;
    }
  }

  public boolean isEnabled() {
    return enabled;
  }

  public StripePaymentIntentResult createPaymentIntent(Payment payment) {
    if (!enabled) {
      log.warn("Stripe fake mode enabled; skipping API call and returning mock payment intent");
      String fakeId = "pi_fake_" + UUID.randomUUID();
      String fakeSecret = "pi_fake_secret_" + UUID.randomUUID();
      return new StripePaymentIntentResult(
        fakeId,
        fakeSecret,
        IntentStatus.SUCCEEDED,
        payment.getGrossAmount(),
        payment.getCurrency().toLowerCase()
      );
    }
    if (apiKey == null || apiKey.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe not configured");
    }
    Map<String, Object> params = new HashMap<>();
    params.put("amount", payment.getGrossAmount());
    params.put("currency", payment.getCurrency().toLowerCase());
    params.put("automatic_payment_methods", Map.of("enabled", true));
    params.put("metadata", Map.of(
      "payment_id", payment.getId().toString(),
      "payment_logical_id", payment.getLogicalId().toString(),
      "user_id", payment.getUserId()
    ));
    try {
      PaymentIntent intent = PaymentIntent.create(params);
      return new StripePaymentIntentResult(
        intent.getId(),
        intent.getClientSecret(),
        mapStatus(intent.getStatus()),
        intent.getAmount(),
        intent.getCurrency()
      );
    } catch (StripeException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe error: " + ex.getMessage());
    }
  }

  private IntentStatus mapStatus(String status) {
    if (status == null) return IntentStatus.REQUIRES_ACTION;
    return switch (status) {
      case "succeeded" -> IntentStatus.SUCCEEDED;
      case "requires_action", "requires_payment_method", "processing" -> IntentStatus.REQUIRES_ACTION;
      default -> IntentStatus.FAILED;
    };
  }

  public record StripePaymentIntentResult(
    String providerReference,
    String clientSecret,
    IntentStatus status,
    long amount,
    String currency
  ) {}
}

