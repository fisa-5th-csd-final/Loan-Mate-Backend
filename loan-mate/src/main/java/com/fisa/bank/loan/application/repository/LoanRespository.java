package com.fisa.bank.loan.application.repository;

import com.fisa.bank.loan.application.domain.Loan;

import java.util.List;

public interface LoanRespository {

    List<Loan> getLoans(Long userId);
}
