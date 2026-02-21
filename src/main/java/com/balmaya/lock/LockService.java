package com.balmaya.lock;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LockService {
  private static final Logger log = LoggerFactory.getLogger(LockService.class);
  private final RedissonClient redisson;

  public <T> T withLock(String key, long waitSeconds, long leaseSeconds, LockedSupplier<T> supplier) {
    RLock lock = redisson.getLock(key);
    try {
      boolean acquired = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
      if (!acquired) {
        log.warn("Lock not acquired for key={} (wait={}s lease={}s)", key, waitSeconds, leaseSeconds);
        throw new IllegalStateException("Resource is busy (lock not acquired): " + key);
      }
      return supplier.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while acquiring lock: " + key, e);
    } finally {
      try {
        if (lock.isHeldByCurrentThread()) lock.unlock();
      } catch (Exception ex) {
        log.warn("Failed to release lock for key={}", key, ex);
      }
    }
  }

  @FunctionalInterface
  public interface LockedSupplier<T> { T get(); }
}

