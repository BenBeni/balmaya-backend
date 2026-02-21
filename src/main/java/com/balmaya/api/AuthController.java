package com.balmaya.api;

import com.balmaya.api.dto.AuthLoginRequest;
import com.balmaya.api.dto.AuthRegisterRequest;
import com.balmaya.api.dto.AuthRegisterResponse;
import com.balmaya.api.dto.AuthTokenResponse;
import com.balmaya.api.dto.AuthUpdatePasswordRequest;
import com.balmaya.api.dto.EmailOtpRequest;
import com.balmaya.api.dto.EmailOtpResponse;
import com.balmaya.api.dto.EmailOtpValidateRequest;
import com.balmaya.api.dto.EmailOtpValidateResponse;
import com.balmaya.api.dto.UserProfileResponse;
import com.balmaya.auth.CurrentUser;
import com.balmaya.auth.KeycloakAuthService;
import com.balmaya.domain.EmailOtp;
import com.balmaya.service.EmailOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
  private static final Logger log = LoggerFactory.getLogger(AuthController.class);
  private final KeycloakAuthService keycloakAuthService;
  private final EmailOtpService emailOtps;

  @PostMapping("/login")
  public AuthTokenResponse login(
      @Valid @RequestBody AuthLoginRequest request,
      @RequestParam(name = "lang", required = false, defaultValue = "en") String language) {
    AuthTokenResponse token = keycloakAuthService.login(request.getUsername(), request.getPassword());
    KeycloakAuthService.KeycloakUserInfo info = keycloakAuthService.userInfo(token.getAccessToken());
    if (info == null || info.getEmail() == null || info.getSub() == null) {
      log.warn("OTP not generated: missing email or user id in userinfo response");
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to generate OTP");
    }
    try {
      emailOtps.generate(info.getEmail(), info.getSub(), language);
    } catch (Exception ex) {
      log.error("OTP generation failed after login", ex);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to generate OTP");
    }
    return token;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthRegisterResponse register(@Valid @RequestBody AuthRegisterRequest request) {
    return keycloakAuthService.register(request);
  }

  @PostMapping("/otp")
  public EmailOtpResponse generateOtp(
      @Valid @RequestBody EmailOtpRequest request,
      @RequestParam(name = "lang", required = false, defaultValue = "en") String language) {
    EmailOtp otp = emailOtps.generate(request.email, request.userId, language);
    return EmailOtpResponse.builder()
      .email(otp.getEmail())
      .code(otp.getCode())
      .expiresAt(otp.getExpiresAt())
      .build();
  }

  @PostMapping("/otp/resend")
  public EmailOtpResponse resendOtp(
      @Valid @RequestBody EmailOtpRequest request,
      @RequestParam(name = "lang", required = false, defaultValue = "en") String language) {
    EmailOtp otp = emailOtps.resend(request.email, request.userId, language);
    return EmailOtpResponse.builder()
      .email(otp.getEmail())
      .code(otp.getCode())
      .expiresAt(otp.getExpiresAt())
      .build();
  }

  @PostMapping("/otp/validate")
  public EmailOtpValidateResponse validateOtp(@Valid @RequestBody EmailOtpValidateRequest request) {
    emailOtps.validate(request.email, request.userId, request.code);
    return EmailOtpValidateResponse.builder().valid(true).build();
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePassword(@Valid @RequestBody AuthUpdatePasswordRequest request, Authentication auth) {
    String userId = CurrentUser.userId(auth);
    keycloakAuthService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
  }

  @GetMapping("/me")
  public UserProfileResponse me(Authentication auth) {
    String userId = CurrentUser.userId(auth);
    return keycloakAuthService.getUserProfile(userId);
  }
}
