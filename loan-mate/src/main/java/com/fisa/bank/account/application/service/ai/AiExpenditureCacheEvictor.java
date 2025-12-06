package com.fisa.bank.account.application.service.ai;

import java.time.YearMonth;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@CacheConfig(cacheNames = "aiExpenditure")
public class AiExpenditureCacheEvictor {

  @CacheEvict(
      key =
          "T(com.fisa.bank.account.application.service.ai.AiExpenditureCacheKey).of(#serviceUserId, #targetMonth)")
  public void evict(Long serviceUserId, YearMonth targetMonth) {
    // Cache eviction handled by annotation
  }
}
