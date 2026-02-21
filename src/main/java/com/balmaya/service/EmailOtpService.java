package com.balmaya.service;

import com.balmaya.domain.EmailOtp;
import com.balmaya.repo.EmailOtpRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmailOtpService {
  private static final SecureRandom RNG = new SecureRandom();

  private final EmailOtpRepository otps;
  private final EmailOtpSender sender;

  @Value("${balmaya.otp.ttl_minutes:5}")
  private long ttlMinutes;

  @Value("${balmaya.otp.resend_interval_seconds:120}")
  private long resendIntervalSeconds;

  public EmailOtp generate(String email, String userId, String language) {
    String lang = normalizeLanguage(language);
    String code = String.format("%06d", RNG.nextInt(1_000_000));
    OffsetDateTime now = OffsetDateTime.now();
    EmailOtp otp = EmailOtp.builder()
      .id(UUID.randomUUID())
      .email(email)
      .userId(userId)
      .code(code)
      .expiresAt(now.plusMinutes(ttlMinutes))
      .lastSentAt(now)
      .build();
    EmailOtp saved = otps.save(otp);
    sender.send(saved, lang);
    return saved;
  }

  @Transactional
  public EmailOtp resend(String email, String userId, String language) {
    String lang = normalizeLanguage(language);
    OffsetDateTime now = OffsetDateTime.now();
    EmailOtp latest = otps.findFirstByEmailAndUserIdOrderByCreatedAtDesc(email, userId).orElse(null);
    if (latest == null || latest.getUsedAt() != null || latest.getExpiresAt().isBefore(now)) {
      return generate(email, userId, lang);
    }

    OffsetDateTime lastSent = latest.getLastSentAt() != null ? latest.getLastSentAt() : latest.getCreatedAt();
    if (lastSent != null && lastSent.plusSeconds(resendIntervalSeconds).isAfter(now)) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "OTP resend too soon");
    }

    latest.setLastSentAt(now);
    EmailOtp saved = otps.save(latest);
    sender.send(saved, lang);
    return saved;
  }

  @Transactional
  public void validate(String email, String userId, String code) {
    OffsetDateTime now = OffsetDateTime.now();
    EmailOtp otp = otps.findFirstByEmailAndUserIdAndCodeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
        email, userId, code, now)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP"));
    otp.setUsedAt(now);
    otps.save(otp);
  }

  private String normalizeLanguage(String language) {
    if (language == null || language.isBlank()) {
      return "en";
    }

    String value = language.trim().toLowerCase(Locale.ROOT);
    if ("en".equals(value) || "english".equals(value)) {
      return "en";
    }
    if ("fr".equals(value) || "french".equals(value)) {
      return "fr";
    }

    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported language. Use english or french");
  }
}

