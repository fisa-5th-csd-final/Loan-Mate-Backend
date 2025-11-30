package com.fisa.bank.account.application.usecase;

import java.math.BigDecimal;
import java.util.Map;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public interface SaveUserSpendingLimitUseCase {

  UserSpendingLimit execute(Map<ConsumptionCategory, BigDecimal> limits);
}
