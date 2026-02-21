package com.balmaya.service;

import com.balmaya.domain.*;
import com.balmaya.domain.Enums.*;
import com.balmaya.lock.LockService;
import com.balmaya.repo.*;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final BeneficiaryRepository beneficiaries;
  private final PaymentRepository payments;
  private final PaymentItemRepository items;
  private final PaymentFeeComponentRepository fees;
  private final PaymentIntentRepository paymentIntents;
  private final FeeCalculator feeCalculator;
  private final LockService locks;
  private final StripePaymentService stripePayments;
  private final LocalPayoutService localPayouts;

  private Beneficiary getCurrentBeneficiaryOwned(UUID beneficiaryLogicalId, String userId) {
    Beneficiary b = beneficiaries.findCurrentByLogicalId(beneficiaryLogicalId)
      .orElseThrow(() -> new EntityNotFoundException("Beneficiary logicalId not found: " + beneficiaryLogicalId));
    if (!b.isCurrent() || !b.getUserId().equals(userId)) throw new EntityNotFoundException("Beneficiary doesn't match");
    return b;
  }

  @Transactional
  public Payment createOneShot(String userId, String actorId, UUID beneficiaryId, PaymentKind kind, UUID scheduleId,
                              OffsetDateTime executeAt, String currency, String note, List<PaymentItem> itemRows, String reason) {
    return createOneShotInternal(userId, actorId, beneficiaryId, kind, scheduleId, executeAt, currency, note, itemRows, reason);
  }

  @Transactional
  public CardPaymentResult createOneShotCard(String userId, String actorId, UUID beneficiaryId, PaymentKind kind, UUID scheduleId,
                                            OffsetDateTime executeAt, String currency, String note, List<PaymentItem> itemRows, String reason) {
    Payment saved = createOneShotInternal(userId, actorId, beneficiaryId, kind, scheduleId, executeAt, currency, note, itemRows, reason);
    StripePaymentService.StripePaymentIntentResult stripeIntent = stripePayments.createPaymentIntent(saved);

    PaymentIntent intent = PaymentIntent.builder()
      .id(UUID.randomUUID())
      .paymentId(saved.getId())
      .provider("STRIPE")
      .providerReference(stripeIntent.providerReference())
      .amount(stripeIntent.amount())
      .currency(stripeIntent.currency())
      .status(stripeIntent.status())
      .build();
    paymentIntents.save(intent);

    if (!stripePayments.isEnabled() && stripeIntent.status() == IntentStatus.SUCCEEDED) {
      localPayouts.executeLocalPayouts(saved.getId());
    }

    return new CardPaymentResult(saved, stripeIntent);
  }

  private Payment createOneShotInternal(String userId, String actorId, UUID beneficiaryId, PaymentKind kind, UUID scheduleId,
                                       OffsetDateTime executeAt, String currency, String note, List<PaymentItem> itemRows, String reason) {

    // lock on beneficiary to reduce duplicate submits for same target; in real life use idempotency keys too
    return locks.withLock("lock:payment-create:" + userId + ":" + beneficiaryId, 5, 30, () -> {
      Beneficiary beneficiary = getCurrentBeneficiaryOwned(beneficiaryId, userId);

      long net = itemRows.stream().mapToLong(PaymentItem::getAmount).sum();

      Payment p = Payment.builder()
        .id(UUID.randomUUID())
        .userId(userId)
        .beneficiaryId(beneficiary.getId())
        .kind(kind)
        .scheduleId(scheduleId)
        .executeAt(executeAt)
        .netAmount(net)
        .feeAmount(0L)
        .grossAmount(net)
        .currency(currency)
        .note(note)
        .status(PaymentStatus.CREATED)
        .build();
      Versioning.initNew(p, actorId);
      p.setChangeReason(reason);

      Payment saved = payments.save(p);

      // snapshot items
      for (PaymentItem pi : itemRows) {
        pi.setId(UUID.randomUUID());
        pi.setPaymentId(saved.getId());
        pi.setStatus(ItemStatus.CREATED);
        Versioning.initNew(pi, actorId);
        pi.setChangeReason(reason);
        items.save(pi);
      }

      // fees
      List<PaymentFeeComponent> feeComps = feeCalculator.computeFeeComponents(saved.getId(), currency, net, actorId);
      long feeTotal = feeComps.stream()
        .filter(fc -> fc.getDirection() == FeeDirection.CHARGE)
        .mapToLong(PaymentFeeComponent::getAmount).sum();

      for (PaymentFeeComponent fc : feeComps) {
        fc.setId(UUID.randomUUID());
        fc.setPaymentId(saved.getId());
        fees.save(fc);
      }

      saved.setFeeAmount(feeTotal);
      saved.setGrossAmount(net + feeTotal);
      saved.setStatus(PaymentStatus.AWAITING_FUNDS);
      return payments.save(saved);
    });
  }

  public List<Payment> listCurrent(String userId) {
    return payments.findCurrentByUser(userId);
  }

  public Payment getCurrentOwned(UUID logicalId, String userId) {
    Payment p = payments.findCurrentByLogicalId(logicalId)
      .orElseThrow(() -> new EntityNotFoundException("Payment logicalId not found: " + logicalId));
    if (!p.getUserId().equals(userId)) throw new EntityNotFoundException("Payment not found");
    return p;
  }

  public List<PaymentItem> getCurrentItems(UUID paymentId) { return items.findCurrentByPaymentId(paymentId); }

  public List<PaymentFeeComponent> getCurrentFees(UUID paymentId) { return fees.findCurrentByPaymentId(paymentId); }

  public List<Payment> listVersions(UUID logicalId) { return payments.findAllVersions(logicalId); }

  public record CardPaymentResult(Payment payment, StripePaymentService.StripePaymentIntentResult stripeIntent) {}
}

