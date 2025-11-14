package com.fisa.bank.loan.application.usecase;

import java.util.List;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.domain.LoanDetail;

public interface ManageLoanUseCase {

  List<Loan> getLoans(Long userId);

  LoanDetail getLoanDetail(Long loanId);
}
