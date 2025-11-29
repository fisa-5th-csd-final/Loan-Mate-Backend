package com.fisa.bank.loan.application.dto.response;

import java.math.BigDecimal;

import com.fisa.bank.loan.persistence.enums.RiskLevel;

public record LoanRiskResponse(BigDecimal risk, RiskLevel riskLevel) {}
