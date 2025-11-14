package com.fisa.bank.loan.persistence.enums;

import lombok.Getter;

@Getter
public enum RiskLevel {
  ONE(1),
  TWO(2),
  THREE(3),
  FOUR(4),
  FIVE(5);

  private final int num;

  RiskLevel(int num) {
    this.num = num;
  }
}
