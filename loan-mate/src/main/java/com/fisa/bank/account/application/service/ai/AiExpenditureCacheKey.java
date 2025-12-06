package com.fisa.bank.account.application.service.ai;

import java.time.YearMonth;

public final class AiExpenditureCacheKey {

  private AiExpenditureCacheKey() {}

  public static String of(Long serviceUserId, YearMonth targetMonth) {
    return serviceUserId + ":" + targetMonth;
  }
}
