package com.fisa.bank.loan.application.service;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.repository.LoanRespository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanService implements ManageLoanUseCase {
    private final LoanRespository loanRespository;
    @Override
    public List<Loan> getLoans(Long userId) {
        loanRespository.getLoans(userId);
        return List.of();
    }
}
