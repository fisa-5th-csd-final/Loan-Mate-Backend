package com.fisa.bank.loan.application.repository;

import java.util.List;

import com.fisa.bank.loan.application.domain.Loan;

public interface LoanRepository {

  List<Loan> getLoans(Long userId);
}
