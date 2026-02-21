package com.balmaya.api;

import com.balmaya.api.dto.*;
import com.balmaya.auth.CurrentUser;
import com.balmaya.domain.Beneficiary;
import com.balmaya.domain.BeneficiaryCategoryRecipient;
import com.balmaya.domain.Enums.BeneficiaryStatus;
import com.balmaya.service.BeneficiaryService;
import jakarta.validation.Valid;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/beneficiaries")
public class BeneficiaryController {

  private final BeneficiaryService service;

  @GetMapping
  public List<BeneficiaryResponse> list(Authentication auth) {
    String userId = CurrentUser.userId(auth);
    return service.listCurrent(userId).stream().map(this::toResp).toList();
  }

  @PostMapping
  public BeneficiaryResponse create(@Valid @RequestBody BeneficiaryCreateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);
    Beneficiary b = service.create(userId, userId, req.fullName, req.relationship, req.phoneE164, req.provider, req.country,
      toRecipients(req.categoryRecipients), req.changeReason);
    return toResp(b);
  }

  @PutMapping("/{logicalId}")
  public BeneficiaryResponse update(@PathVariable("logicalId") UUID logicalId, @Valid @RequestBody BeneficiaryUpdateRequest req, Authentication auth) {
    String userId = CurrentUser.userId(auth);
    BeneficiaryStatus st = BeneficiaryStatus.valueOf(Optional.ofNullable(req.status).orElse("ACTIVE"));
    Beneficiary b = service.update(logicalId, userId, userId, req.fullName, req.relationship, req.phoneE164, req.provider, req.country, st,
      toRecipients(req.categoryRecipients), req.changeReason);
    return toResp(b);
  }

  private BeneficiaryResponse toResp(Beneficiary b) {
    List<BeneficiaryCategoryRecipientResponse> recipients = service.getCurrentCategoryRecipients(b.getId()).stream()
      .map(this::toRecipientResp)
      .toList();
    return BeneficiaryResponse.builder()
      .id(b.getId())
      .logicalId(b.getLogicalId())
      .version(b.getVersion())
      .isCurrent(b.isCurrent())
      .createdAt(b.getCreatedAt())
      .createdBy(b.getCreatedBy())
      .changeReason(b.getChangeReason())
      .fullName(b.getFullName())
      .relationship(b.getRelationship())
      .country(b.getCountry())
      .provider(b.getProvider())
      .phoneE164(b.getPhoneE164())
      .status(b.getStatus())
      .categoryRecipients(recipients)
      .build();
  }

  private List<BeneficiaryCategoryRecipient> toRecipients(List<BeneficiaryCategoryRecipientRequest> reqs) {
    if (reqs == null) return List.of();
    return reqs.stream()
      .map(r -> BeneficiaryCategoryRecipient.builder()
        .category(r.category)
        .recipientName(r.recipientName)
        .recipientPhoneE164(r.recipientPhoneE164)
        .recipientEmail(r.recipientEmail)
        .recipientAccountNumber(r.recipientAccountNumber)
        .provider(r.provider)
        .build()
      ).toList();
  }

  private BeneficiaryCategoryRecipientResponse toRecipientResp(BeneficiaryCategoryRecipient r) {
    return BeneficiaryCategoryRecipientResponse.builder()
      .category(r.getCategory())
      .recipientName(r.getRecipientName())
      .recipientPhoneE164(r.getRecipientPhoneE164())
      .recipientEmail(r.getRecipientEmail())
      .recipientAccountNumber(r.getRecipientAccountNumber())
      .provider(r.getProvider())
      .build();
  }
}

