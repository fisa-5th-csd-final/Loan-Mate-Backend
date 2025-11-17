package com.fisa.bank.loan.application.dto.response;

import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.persistence.enums.RiskLevel;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailResponse {
    private final Long loanId;
    private final String loanName;
    private final BigDecimal remainPrincipal;
    private final BigDecimal principal;
    private final BigDecimal monthlyRepayment;
    private final String accountNumber;
    private final LoanType loanType;
    private final RepaymentType repaymentType;

    // TODO: 나중에 위험도 계산 가능하면 final로 변경
    private RiskLevel riskLevel;

    public static LoanDetailResponse from(Long loanId, LoanDetail loanDetail) {
        // TODO: 위험도 세팅
        return LoanDetailResponse.builder()
                .loanId(loanId)
                .loanName(loanDetail.getName())
                .loanType(loanDetail.getLoanType())
                .monthlyRepayment(loanDetail.getMonthlyRepayment())
                .repaymentType(loanDetail.getRepaymentType())
                .accountNumber(loanDetail.getAccountNumber())
                .remainPrincipal(loanDetail.getRemainPrincipal())
                .principal(loanDetail.getPrincipal())
                .build();
    }
}