package com.balmaya.jobs;

import com.balmaya.repo.EmailOtpRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpCleanupJob {
  private final EmailOtpRepository otps;

  @Scheduled(fixedDelayString = "${balmaya.otp.cleanup_interval_ms:60000}")
  @Transactional
  public void deleteExpiredOtps() {
    otps.deleteByExpiresAtBefore(OffsetDateTime.now());
  }
}

