package com.fisa.bank.loan.persistence.repository;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.repository.LoanRespository;
import com.fisa.bank.loan.persistence.entity.LoanEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LoanRepositoryImpl implements LoanRespository {
    private final JpaLoanRepository jpaLoanRepository;

    @Override
    public List<Loan> getLoans(Long userId) {
        List<Loan> loans = jpaLoanRepository.findAllByUserId(userId).stream().map(LoanEntity::toDomain).toList();
        return loans;
    }
}
