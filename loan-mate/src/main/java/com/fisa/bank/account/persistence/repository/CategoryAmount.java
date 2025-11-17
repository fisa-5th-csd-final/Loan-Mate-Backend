package com.fisa.bank.account.persistence.repository;

import java.math.BigDecimal;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public class CategoryAmount {

  private final ConsumptionCategory category;
  private final BigDecimal total;

  public CategoryAmount(ConsumptionCategory category, BigDecimal total) {
    this.category = category;
    this.total = total;
  }

  public ConsumptionCategory getCategory() {
    return category;
  }

  public BigDecimal getTotal() {
    return total;
  }
}
