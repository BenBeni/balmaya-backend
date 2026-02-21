package com.balmaya.api.dto;

import com.balmaya.domain.Enums.ItemCategory;
import com.balmaya.domain.Enums.Provider;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryCategoryRecipientRequest {
  @NotNull public ItemCategory category;
  @NotBlank public String recipientName;
  @NotBlank public String recipientPhoneE164;
  public String recipientEmail;
  public String recipientAccountNumber;
  @NotNull public Provider provider;
}

