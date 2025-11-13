package com.fisa.bank.loan.application.domain;

import com.fisa.bank.loan.persistence.enums.LoanType;
import com.fisa.bank.loan.persistence.enums.RepaymentType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LoanDetail {
    private BigDecimal remainPrincipal;
    private BigDecimal principal;
    private BigDecimal monthlyRepayment;
    private String accountNumber;
    private LoanType loanType;
    private RepaymentType repaymentType;

}
