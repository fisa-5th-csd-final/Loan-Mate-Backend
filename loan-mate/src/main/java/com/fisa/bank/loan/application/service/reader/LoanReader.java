package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.exception.LoanLedgerNotFoundException;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.persistence.repository.LoanRepositoryImpl;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.entity.id.LoanLedgerId;
import com.fisa.bank.persistence.loan.repository.LoanLedgerRepository;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReader {
  private final LoanRepository loanRepository;
  private final CoreBankingClient coreBankingClient;
  private final LoanLedgerRepository loanLedgerRepository;

  public LoanDetail findLoanDetail(Long loanId) {
    String url = "/loans/ledger/" + loanId;

    return coreBankingClient.fetchOne(url, LoanDetail.class);
  }

  public List<Loan> findLoans(Long userId) {
    return loanRepository.getLoans(UserId.of(userId));
  }

  public Loan findLoanById(Long loanId) {
    LoanLedger ledger =
        loanLedgerRepository
            .findById(LoanLedgerId.of(loanId))
            .orElseThrow(() -> new LoanLedgerNotFoundException(loanId));

    return LoanRepositoryImpl.toDomain(ledger);
  }
}
