package com.balmaya.api.admin;

import com.balmaya.api.dto.LocalPayoutAccountRequest;
import com.balmaya.api.dto.LocalPayoutAccountResponse;
import com.balmaya.domain.LocalPayoutAccount;
import com.balmaya.repo.LocalPayoutAccountRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AdminLocalPayoutAccountController {
  private final LocalPayoutAccountRepository accounts;

  @GetMapping("/admin/read/local-accounts")
  public List<LocalPayoutAccountResponse> list() {
    return accounts.findAll().stream().map(this::toResponse).toList();
  }

  @PostMapping("/admin/write/local-accounts")
  @ResponseStatus(HttpStatus.CREATED)
  public LocalPayoutAccountResponse create(@Valid @RequestBody LocalPayoutAccountRequest request) {
    LocalPayoutAccount account = LocalPayoutAccount.builder()
      .id(UUID.randomUUID())
      .provider(request.provider)
      .country(request.country)
      .accountName(request.accountName)
      .accountReference(request.accountReference)
      .status(request.status)
      .build();
    return toResponse(accounts.save(account));
  }

  @PutMapping("/admin/write/local-accounts/{id}")
  public LocalPayoutAccountResponse update(@PathVariable UUID id, @Valid @RequestBody LocalPayoutAccountRequest request) {
    LocalPayoutAccount account = accounts.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Local payout account not found: " + id));
    account.setProvider(request.provider);
    account.setCountry(request.country);
    account.setAccountName(request.accountName);
    account.setAccountReference(request.accountReference);
    account.setStatus(request.status);
    return toResponse(accounts.save(account));
  }

  private LocalPayoutAccountResponse toResponse(LocalPayoutAccount account) {
    return LocalPayoutAccountResponse.builder()
      .id(account.getId())
      .provider(account.getProvider())
      .country(account.getCountry())
      .accountName(account.getAccountName())
      .accountReference(account.getAccountReference())
      .status(account.getStatus())
      .createdAt(account.getCreatedAt())
      .updatedAt(account.getUpdatedAt())
      .build();
  }
}

