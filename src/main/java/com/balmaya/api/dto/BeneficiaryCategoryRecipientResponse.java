package com.balmaya.api.dto;

import com.balmaya.domain.Enums.ItemCategory;
import com.balmaya.domain.Enums.Provider;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryCategoryRecipientResponse {
  public ItemCategory category;
  public String recipientName;
  public String recipientPhoneE164;
  public String recipientEmail;
  public String recipientAccountNumber;
  public Provider provider;
}

