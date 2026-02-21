package com.balmaya.api.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {
  public String userId;
  public String username;
  public String email;
  public String firstName;
  public String lastName;
  public String phoneNumber;
}

