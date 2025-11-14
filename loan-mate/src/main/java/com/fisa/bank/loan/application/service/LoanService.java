package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.repository.LoanRespository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanService implements ManageLoanUseCase {
  private final LoanRespository loanRespository;

  @Override
  public List<Loan> getLoans(Long userId) {
    List<Loan> loans = loanRespository.getLoans(userId);
    return loans;
  }
}
