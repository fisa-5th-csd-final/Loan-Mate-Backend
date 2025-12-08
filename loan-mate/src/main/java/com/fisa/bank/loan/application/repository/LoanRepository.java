package com.fisa.bank.loan.application.repository;

import java.util.List;
import java.util.Optional;

import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface LoanRepository {

  List<Loan> getLoans(UserId userId);

  List<Loan> getLoansNonTerminated(UserId userId);

  Optional<Loan> findById(Long loanId);
}
