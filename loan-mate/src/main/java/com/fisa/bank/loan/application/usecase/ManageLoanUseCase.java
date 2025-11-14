package com.fisa.bank.loan.application.usecase;

import java.util.List;

import com.fisa.bank.loan.application.domain.Loan;

public interface ManageLoanUseCase {

  List<Loan> getLoans(Long userId);
}
