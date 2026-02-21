package com.balmaya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailOtpValidateRequest {
  @NotBlank @Email public String email;
  @NotBlank public String userId;
  @NotBlank public String code;
}

