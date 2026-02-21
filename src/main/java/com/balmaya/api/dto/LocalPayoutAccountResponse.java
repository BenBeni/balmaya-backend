package com.balmaya.api.dto;

import com.balmaya.domain.Enums.Provider;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LocalPayoutAccountResponse {
  private UUID id;
  private Provider provider;
  private String country;
  private String accountName;
  private String accountReference;
  private String status;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}

