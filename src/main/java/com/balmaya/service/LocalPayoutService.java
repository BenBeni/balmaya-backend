package com.balmaya.service;

import com.balmaya.domain.Beneficiary;
import com.balmaya.domain.BeneficiaryCategoryRecipient;
import com.balmaya.domain.Enums.ItemCategory;
import com.balmaya.domain.Enums.PaymentStatus;
import com.balmaya.domain.Enums.Provider;
import com.balmaya.domain.LocalPayoutAccount;
import com.balmaya.domain.Payment;
import com.balmaya.domain.PaymentItem;
import com.balmaya.repo.BeneficiaryCategoryRecipientRepository;
import com.balmaya.repo.BeneficiaryRepository;
import com.balmaya.repo.LocalPayoutAccountRepository;
import com.balmaya.repo.PaymentItemRepository;
import com.balmaya.repo.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalPayoutService {
  private static final Logger log = LoggerFactory.getLogger(LocalPayoutService.class);

  private final PaymentRepository payments;
  private final PaymentItemRepository items;
  private final BeneficiaryRepository beneficiaries;
  private final BeneficiaryCategoryRecipientRepository categoryRecipients;
  private final LocalPayoutAccountRepository localAccounts;

  @Transactional
  public void executeLocalPayouts(UUID paymentId) {
    Payment payment = payments.findById(paymentId)
      .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));
    if (!payment.isCurrent()) return;
    if (payment.getStatus() == PaymentStatus.FUNDED) return;

    Beneficiary beneficiary = beneficiaries.findById(payment.getBeneficiaryId())
      .orElseThrow(() -> new EntityNotFoundException("Beneficiary not found: " + payment.getBeneficiaryId()));

    List<BeneficiaryCategoryRecipient> recipients = categoryRecipients.findCurrentByBeneficiaryId(beneficiary.getId());
    Map<ItemCategory, Provider> providerByCategory = new HashMap<>();
    for (BeneficiaryCategoryRecipient r : recipients) {
      providerByCategory.put(r.getCategory(), r.getProvider());
    }

    List<PaymentItem> paymentItems = items.findCurrentByPaymentId(payment.getId());
    for (PaymentItem item : paymentItems) {
      Provider provider = providerByCategory.get(item.getCategory());
      if (provider == null) {
        throw new EntityNotFoundException("Provider not found for category: " + item.getCategory());
      }
      LocalPayoutAccount account = localAccounts.findByProviderAndCountry(provider, beneficiary.getCountry())
        .orElseThrow(() -> new EntityNotFoundException("Local payout account missing for provider " + provider + " in " + beneficiary.getCountry()));

      // Placeholder for real provider integration
      log.info("Local payout: payment {} item {} using {} account {} in {}",
        payment.getId(), item.getId(), provider, account.getAccountReference(), beneficiary.getCountry());
    }

    markFunded(payment, "system", "Local payout success");
  }

  @Transactional
  public Payment markFunded(Payment current, String actorId, String reason) {
    if (!current.isCurrent()) return current;
    if (current.getStatus() == PaymentStatus.FUNDED) return current;
    current.setCurrent(false);
    payments.saveAndFlush(current);

    Payment next = Payment.builder()
      .id(UUID.randomUUID())
      .userId(current.getUserId())
      .beneficiaryId(current.getBeneficiaryId())
      .kind(current.getKind())
      .scheduleId(current.getScheduleId())
      .executeAt(current.getExecuteAt())
      .netAmount(current.getNetAmount())
      .feeAmount(current.getFeeAmount())
      .grossAmount(current.getGrossAmount())
      .currency(current.getCurrency())
      .note(current.getNote())
      .status(PaymentStatus.FUNDED)
      .build();
    Versioning.nextVersion(next, current, actorId, reason);
    return payments.save(next);
  }
}

