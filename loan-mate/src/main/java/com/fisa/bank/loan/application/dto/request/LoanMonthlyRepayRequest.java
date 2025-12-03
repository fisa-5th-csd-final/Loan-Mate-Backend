package com.fisa.bank.loan.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanMonthlyRepayRequest {
  @NotNull private BigDecimal amount;
}
