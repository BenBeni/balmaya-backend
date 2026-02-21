package com.balmaya.api;

import com.balmaya.api.dto.*;
import com.balmaya.auth.CurrentUser;
import com.balmaya.domain.*;
import com.balmaya.service.PaymentService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

  private final PaymentService service;

  @GetMapping
  public List<PaymentResponse> list(Authentication auth) {
    String userId = CurrentUser.userId(auth);
    return service.listCurrent(userId).stream().map(p -> toResp(p, service.getCurrentItems(p.getId()), service.getCurrentFees(p.getId()))).toList();
  }

  @PostMapping
  public PaymentResponse create(@Valid @RequestBody PaymentCreateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);

    List<PaymentItem> items = new ArrayList<>();
    for (PaymentItemRequest ir : req.items) {
      PaymentItem pi = PaymentItem.builder()
        .category(ir.category)
        .amount(ir.amount)
        .currency(ir.currency)
        .method(ir.method)
        .billerCode(ir.billerCode)
        .billerAccountRef(ir.billerAccountRef)
        .status(com.balmaya.domain.Enums.ItemStatus.CREATED)
        .build();
      items.add(pi);
    }

    Payment p = service.createOneShot(userId, userId, req.beneficiaryId, req.kind, req.scheduleId, req.executeAt, req.currency, req.note, items, req.changeReason);
    return toResp(p, service.getCurrentItems(p.getId()), service.getCurrentFees(p.getId()));
  }

  @PostMapping("/card")
  public PaymentCardResponse createCard(@Valid @RequestBody PaymentCreateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);

    List<PaymentItem> items = new ArrayList<>();
    for (PaymentItemRequest ir : req.items) {
      PaymentItem pi = PaymentItem.builder()
        .category(ir.category)
        .amount(ir.amount)
        .currency(ir.currency)
        .method(ir.method)
        .billerCode(ir.billerCode)
        .billerAccountRef(ir.billerAccountRef)
        .status(com.balmaya.domain.Enums.ItemStatus.CREATED)
        .build();
      items.add(pi);
    }

    PaymentService.CardPaymentResult result = service.createOneShotCard(
      userId, userId, req.beneficiaryId, req.kind, req.scheduleId, req.executeAt, req.currency, req.note, items, req.changeReason
    );
    Payment payment = result.payment();
    PaymentResponse paymentResponse = toResp(payment, service.getCurrentItems(payment.getId()), service.getCurrentFees(payment.getId()));
    return PaymentCardResponse.builder()
      .payment(paymentResponse)
      .stripePaymentIntentId(result.stripeIntent().providerReference())
      .stripeClientSecret(result.stripeIntent().clientSecret())
      .build();
  }

  private PaymentResponse toResp(Payment p, List<PaymentItem> items, List<PaymentFeeComponent> fees) {
    return PaymentResponse.builder()
      .id(p.getId())
      .logicalId(p.getLogicalId())
      .version(p.getVersion())
      .isCurrent(p.isCurrent())
      .beneficiaryId(p.getBeneficiaryId())
      .kind(p.getKind())
      .scheduleId(p.getScheduleId())
      .executeAt(p.getExecuteAt())
      .netAmount(p.getNetAmount())
      .feeAmount(p.getFeeAmount())
      .grossAmount(p.getGrossAmount())
      .currency(p.getCurrency())
      .status(p.getStatus())
      .note(p.getNote())
      .items(items.stream().map(i -> PaymentResponse.PaymentItemView.builder()
        .id(i.getId())
        .category(i.getCategory())
        .amount(i.getAmount())
        .currency(i.getCurrency())
        .method(i.getMethod())
        .status(i.getStatus())
        .build()).toList())
      .fees(fees.stream().map(f -> PaymentResponse.FeeComponentView.builder()
        .id(f.getId())
        .code(f.getCode())
        .amount(f.getAmount())
        .currency(f.getCurrency())
        .direction(f.getDirection())
        .build()).toList())
      .build();
  }
}

