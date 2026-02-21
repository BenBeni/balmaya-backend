package com.balmaya.api.dto;

import java.time.OffsetDateTime;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailOtpResponse {
  public String email;
  public String code;
  public OffsetDateTime expiresAt;
}

