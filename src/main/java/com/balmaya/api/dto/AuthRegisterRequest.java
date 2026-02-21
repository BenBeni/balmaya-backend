package com.balmaya.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRegisterRequest {
  @NotBlank
  private String username;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
      message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special character")
  private String password;

  @NotBlank
  private String phoneNumber;

  private String firstName;
  private String lastName;
}

