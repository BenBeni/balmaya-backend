package com.balmaya.repo;

import com.balmaya.domain.EmailOtp;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
  Optional<EmailOtp> findFirstByEmailAndUserIdAndCodeAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
    String email, String userId, String code, OffsetDateTime now
  );

  Optional<EmailOtp> findFirstByEmailAndUserIdOrderByCreatedAtDesc(String email, String userId);

  int deleteByExpiresAtBefore(OffsetDateTime cutoff);
}

