package com.balmaya.api.dto;

import com.balmaya.domain.Enums.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryCreateRequest {
  @NotBlank public String fullName;
  public String relationship;
  @NotBlank public String phoneE164;
  @NotNull public Provider provider;
  @NotBlank public String country;
  @NotEmpty @Valid public List<BeneficiaryCategoryRecipientRequest> categoryRecipients;
  public String changeReason;
}

