package com.fisa.bank.loan.application.dto.response;

import java.math.BigDecimal;

public record LoanRepaymentRatioResponse(
    BigDecimal monthlyIncome,
    BigDecimal totalMonthlyRepayment,
    BigDecimal ratio,
    BigDecimal peerAverageRatio) {}
