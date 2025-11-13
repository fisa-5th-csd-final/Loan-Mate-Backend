package com.fisa.bank.loan.application.service;

import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.repository.LoanRespository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.loan.persistence.enums.RiskLevel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class LoanService implements ManageLoanUseCase {
    private final LoanRespository loanRespository;
    private final CoreBankingClient coreBankingClient;

    @Override
    public List<Loan> getLoans(Long userId) {
        List<Loan> loans = loanRespository.getLoans(userId);
        return loans;
    }
}
