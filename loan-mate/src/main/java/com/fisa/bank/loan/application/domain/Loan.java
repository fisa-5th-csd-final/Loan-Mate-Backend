package com.fisa.bank.loan.application.domain;

import com.fisa.bank.loan.persistence.enums.RiskLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Loan {
    private final String loanName;
    private RiskLevel riskLevel;
}
