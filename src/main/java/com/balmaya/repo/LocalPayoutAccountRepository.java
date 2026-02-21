package com.balmaya.repo;

import com.balmaya.domain.Enums.Provider;
import com.balmaya.domain.LocalPayoutAccount;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalPayoutAccountRepository extends JpaRepository<LocalPayoutAccount, UUID> {
  Optional<LocalPayoutAccount> findByProviderAndCountry(Provider provider, String country);
}

