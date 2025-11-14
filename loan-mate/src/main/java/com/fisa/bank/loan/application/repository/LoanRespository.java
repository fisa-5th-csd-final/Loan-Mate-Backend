package com.fisa.bank.loan.application.repository;

import java.util.List;

import com.fisa.bank.loan.application.domain.Loan;

public interface LoanRespository {

  List<Loan> getLoans(Long userId);
}
