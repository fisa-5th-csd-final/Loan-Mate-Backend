package com.fisa.bank.loan.application.usecase;

import java.util.List;

import com.fisa.bank.loan.application.dto.response.*;

public interface ManageLoanUseCase {

  List<LoanListResponse> getLoans();

  LoanDetailResponse getLoanDetail(Long loanId);

  LoanAutoDepositResponse getAutoDeposit(Long loanId);

  void cancelLoan(Long loanId);

  void updateAutoDepositEnabled(Long loanId, boolean autoDepositEnabled);

  List<LoansWithPrepaymentBenefitResponse> getLoansWithPrepaymentBenefit();

  List<AutoDepositResponse> getAutoDepositSummary();

  List<LoanDetailResponse> getLoanDetails();
}
