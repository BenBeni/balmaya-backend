package com.balmaya.api.dto;

import com.balmaya.domain.Enums.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BeneficiaryResponse {
  public UUID id;
  public UUID logicalId;
  public int version;
  public boolean isCurrent;
  public OffsetDateTime createdAt;
  public String createdBy;
  public String changeReason;

  public String fullName;
  public String relationship;
  public String country;
  public Provider provider;
  public String phoneE164;
  public BeneficiaryStatus status;
  public List<BeneficiaryCategoryRecipientResponse> categoryRecipients;
}

