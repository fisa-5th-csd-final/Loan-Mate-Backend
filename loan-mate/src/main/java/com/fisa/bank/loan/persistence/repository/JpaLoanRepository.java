package com.fisa.bank.loan.persistence.repository;

import com.fisa.bank.loan.persistence.entity.LoanEntity;
import com.fisa.bank.loan.persistence.entity.id.LoanId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaLoanRepository extends JpaRepository<LoanEntity, LoanId> {
    List<LoanEntity> findAllByUserId(Long userId);
}
