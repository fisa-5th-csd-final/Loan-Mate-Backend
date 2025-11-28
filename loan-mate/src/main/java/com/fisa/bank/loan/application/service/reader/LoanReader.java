package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.loan.application.client.LoanCoreBankingClient;
import com.fisa.bank.loan.application.exception.LoanLedgerNotFoundException;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.repository.LoanLedgerRepository;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReader {
  private final LoanRepository loanRepository;
  private final LoanLedgerRepository loanLedgerRepository;
  private final LoanCoreBankingClient loanCoreBankingClient;

  public LoanDetail findLoanDetail(Long loanId) { // 특정 LoanLedger의 정보
    return loanCoreBankingClient.fetchLoanDetail(loanId);
  }

  public List<Loan> findLoans(Long userId) {
    return loanRepository.getLoans(UserId.of(userId));
  }

  public Loan findLoanById(Long loanId) {
    return loanRepository
        .findById(loanId)
        .orElseThrow(() -> new LoanLedgerNotFoundException(loanId));
  }

  public List<PrepaymentInfo> findPrepaymentInfos() {
    return loanCoreBankingClient.fetchPrepaymentInfos();
  }

  public List<LoanLedger> findAllByUserId(Long userId) {
    return loanLedgerRepository.findAllByUser_UserId(UserId.of(userId));
  }
}
