package com.balmaya.service;

import com.balmaya.domain.EmailOtp;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOtpSender {
  private static final Logger log = LoggerFactory.getLogger(EmailOtpSender.class);
  private final JavaMailSender mailSender;

  @Value("${balmaya.otp.email.from:no-reply@balmaya.local}")
  private String from;

  @Value("${balmaya.otp.email.subject.en:Your verification code}")
  private String subjectEn;

  @Value("${balmaya.otp.email.subject.fr:Votre code de verification}")
  private String subjectFr;

  public void send(EmailOtp otp, String language) {
    MimeMessage msg = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");
      helper.setFrom(from);
      helper.setTo(otp.getEmail());
      helper.setSubject("fr".equals(language) ? subjectFr : subjectEn);
      helper.setText(buildBody(otp, language), false);
      mailSender.send(msg);
    } catch (MessagingException ex) {
      log.error("Failed to send OTP email to {}", otp.getEmail(), ex);
      throw new IllegalStateException("Failed to send OTP email", ex);
    }
  }

  private String buildBody(EmailOtp otp, String language) {
    long expiresInMinutes = Math.max(1, java.time.Duration.between(OffsetDateTime.now(), otp.getExpiresAt()).toMinutes());
    if ("fr".equals(language)) {
      return "Votre code de verification est : " + otp.getCode() + "\n\nExpire dans " + expiresInMinutes + " minutes.";
    }
    return "Your verification code is: " + otp.getCode() + "\n\nExpires in " + expiresInMinutes + " minutes.";
  }
}
