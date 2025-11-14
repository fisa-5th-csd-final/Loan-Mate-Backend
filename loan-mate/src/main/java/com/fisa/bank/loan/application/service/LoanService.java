package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.domain.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanService implements ManageLoanUseCase {
  private final LoanRepository loanRepository;
  private final CoreBankingClient coreBankingClient;

  @Override
  public List<Loan> getLoans(Long userId) {
    List<Loan> loans = loanRepository.getLoans(userId);
    return loans;
  }

  @Override
  public LoanDetail getLoanDetail(Long loanId) {
    LoanDetail loanDetail = coreBankingClient.fetchLoanDetail(loanId);
    return loanDetail;
  }
}
