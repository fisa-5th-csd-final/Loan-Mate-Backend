package com.fisa.bank.account.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.account.persistence.entity.AutoDepositHistory;

public interface JpaAutoDepositHistoryRepository extends JpaRepository<AutoDepositHistory, Long> {

  List<AutoDepositHistory> findByLoanLedgerId(Long loanLedgerId);
}
