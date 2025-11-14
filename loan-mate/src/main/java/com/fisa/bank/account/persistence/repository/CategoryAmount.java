package com.fisa.bank.account.persistence.repository;

import java.math.BigDecimal;

import com.fisa.bank.account.application.domain.ConsumptionCategory;

public interface CategoryAmount {
  ConsumptionCategory getCategory();

  BigDecimal getTotal();
}
