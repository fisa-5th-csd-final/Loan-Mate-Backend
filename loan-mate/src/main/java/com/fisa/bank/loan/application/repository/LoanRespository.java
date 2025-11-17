package com.fisa.bank.loan.application.repository;

import java.util.List;

import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.persistence.user.entity.id.UserId;

public interface LoanRespository {

  List<Loan> getLoans(UserId userId);
}
