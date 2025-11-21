package com.fisa.bank.loan.application.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class InterestDetail {
  private final LocalDateTime repaymentDate;
  private final BigDecimal interest;
}
