package com.fisa.bank.account.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.persistence.loan.entity.LoanLedger;

public interface LoanLedgerRepository extends JpaRepository<LoanLedger, Long> {

  List<LoanLedger> findByAutoDepositEnabledTrueAndNextRepaymentDate(LocalDate date);
}
