package com.fisa.bank.loan.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.persistence.entity.LoanEntity;

@Repository
@RequiredArgsConstructor
public class LoanRepositoryImpl implements LoanRepository {
  private final JpaLoanRepository jpaLoanRepository;

  @Override
  public List<Loan> getLoans(Long userId) {
    List<Loan> loans =
        jpaLoanRepository.findAllByUserId(userId).stream().map(LoanEntity::toDomain).toList();
    return loans;
  }
}
