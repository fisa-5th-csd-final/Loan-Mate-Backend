package com.fisa.bank.loan.persistence.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum RiskLevel {
  ONE,
  TWO,
  THREE,
  FOUR,
  FIVE;

  public static RiskLevel fromRiskScore(BigDecimal risk) {
    if (risk == null) {
      return null;
    }

    // BigDecimal을 double로 변환 (0.0 ~ 1.0)
    double r = risk.doubleValue();

    if (r < 0.2) {
      return ONE;
    } else if (r < 0.4) {
      return TWO;
    } else if (r < 0.6) {
      return THREE;
    } else if (r < 0.8) {
      return FOUR;
    } else {
      return FIVE;
    }
  }
}
