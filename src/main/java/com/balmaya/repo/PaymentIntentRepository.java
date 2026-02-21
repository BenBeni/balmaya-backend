package com.balmaya.repo;

import com.balmaya.domain.PaymentIntent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, UUID> {
  Optional<PaymentIntent> findByProviderAndProviderReference(String provider, String providerReference);
}

