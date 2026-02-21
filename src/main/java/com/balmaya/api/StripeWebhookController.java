package com.balmaya.api;

import com.balmaya.domain.Enums.IntentStatus;
import com.balmaya.domain.PaymentIntent;
import com.balmaya.repo.PaymentIntentRepository;
import com.balmaya.service.LocalPayoutService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

  private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

  private final PaymentIntentRepository paymentIntents;
  private final LocalPayoutService localPayouts;

  @Value("${balmaya.stripe.webhook_secret:}")
  private String webhookSecret;

  @PostMapping
  public void handle(HttpServletRequest request) {
    if (webhookSecret == null || webhookSecret.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Stripe webhook not configured");
    }
    String payload;
    try {
      StringBuilder sb = new StringBuilder();
      request.getReader().lines().forEach(sb::append);
      payload = sb.toString();
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload");
    }

    String sigHeader = request.getHeader("Stripe-Signature");
    Event event;
    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (SignatureVerificationException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid signature");
    }

    switch (event.getType()) {
      case "payment_intent.succeeded":
      case "payment_intent.payment_failed":
      case "payment_intent.processing":
      case "payment_intent.requires_action":
      case "payment_intent.amount_capturable_updated":
        handlePaymentIntentEvent(event);
        break;
      case "charge.succeeded":
      case "charge.refunded":
        handleChargeEvent(event);
        break;
      default:
        log.debug("Unhandled Stripe event type: {}", event.getType());
    }
  }

  private void handlePaymentIntentEvent(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Optional<StripeObject> obj = dataObjectDeserializer.getObject();
    if (obj.isEmpty() || !(obj.get() instanceof com.stripe.model.PaymentIntent stripeIntent)) {
      log.warn("Stripe event deserialization failed: {}", event.getId());
      return;
    }
    paymentIntents.findByProviderAndProviderReference("STRIPE", stripeIntent.getId())
      .ifPresent(intent -> applyStripeUpdate(intent, stripeIntent));
  }

  private void applyStripeUpdate(PaymentIntent intent, com.stripe.model.PaymentIntent stripeIntent) {
    intent.setStatus(mapStatus(stripeIntent.getStatus()));
    if (stripeIntent.getLatestCharge() != null) {
      intent.setProviderChargeReference(stripeIntent.getLatestCharge());
    }
    paymentIntents.save(intent);
    if (intent.getStatus() == IntentStatus.SUCCEEDED) {
      localPayouts.executeLocalPayouts(intent.getPaymentId());
    }
  }

  private void handleChargeEvent(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Optional<StripeObject> obj = dataObjectDeserializer.getObject();
    if (obj.isEmpty() || !(obj.get() instanceof com.stripe.model.Charge charge)) {
      log.warn("Stripe charge event deserialization failed: {}", event.getId());
      return;
    }
    String paymentIntentId = charge.getPaymentIntent();
    if (paymentIntentId == null || paymentIntentId.isBlank()) {
      return;
    }
    paymentIntents.findByProviderAndProviderReference("STRIPE", paymentIntentId)
      .ifPresent(intent -> applyStripeChargeUpdate(intent, charge));
  }

  private void applyStripeChargeUpdate(PaymentIntent intent, com.stripe.model.Charge charge) {
    intent.setProviderChargeReference(charge.getId());
    if (charge.getRefunds() != null && !charge.getRefunds().getData().isEmpty()) {
      intent.setProviderRefundReference(charge.getRefunds().getData().get(0).getId());
    }
    paymentIntents.save(intent);
  }

  private IntentStatus mapStatus(String status) {
    if (status == null) return IntentStatus.REQUIRES_ACTION;
    return switch (status) {
      case "succeeded" -> IntentStatus.SUCCEEDED;
      case "requires_action", "requires_payment_method", "processing" -> IntentStatus.REQUIRES_ACTION;
      default -> IntentStatus.FAILED;
    };
  }
}

