package com.balmaya.api.dto;

import com.balmaya.domain.Enums.Provider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocalPayoutAccountRequest {
  @NotNull public Provider provider;
  @NotBlank public String country;
  @NotBlank public String accountName;
  @NotBlank public String accountReference;
  @NotBlank public String status;
}

