package com.fisa.bank.loan.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.loan.persistence.entity.LoanEntity;
import com.fisa.bank.loan.persistence.entity.id.LoanId;

public interface JpaLoanRepository extends JpaRepository<LoanEntity, LoanId> {
  List<LoanEntity> findAllByUserId(Long userId);
}
