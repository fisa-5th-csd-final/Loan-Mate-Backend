package com.fisa.bank.loan.application.usecase;

import java.util.List;

import com.fisa.bank.loan.application.dto.response.LoanAutoDepositResponse;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;

public interface ManageLoanUseCase {

  List<LoanListResponse> getLoans(Long userId);

  LoanDetailResponse getLoanDetail(Long loanId);

  LoanAutoDepositResponse getAutoDeposit(Long loanId);

  void cancelLoan(Long loanId);
}
