package com.fisa.bank.loan.application.usecase;

import com.fisa.bank.loan.application.domain.Loan;

import java.util.List;

public interface ManageLoanUseCase {

    List<Loan> getLoans(Long userId);
}
