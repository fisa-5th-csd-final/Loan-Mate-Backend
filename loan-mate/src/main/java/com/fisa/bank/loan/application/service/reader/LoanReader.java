package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
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
  private final CoreBankingClient coreBankingClient;

  private static final String PREPAYMENT_INFOS_URL = "/loans/prepayment-infos";

  public LoanDetail findLoanDetail(Long loanId) {
    String url = "/loans/ledger/" + loanId;

    return coreBankingClient.fetchOne(url, LoanDetail.class);
  }

  public List<LoanDetail> findLoanDetails() {
    String url = "/loans/ledgers/details";

    return coreBankingClient.fetchList(url, LoanDetail.class);
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
    return coreBankingClient.fetchList(PREPAYMENT_INFOS_URL, PrepaymentInfo.class);
  }

  public List<LoanLedger> findAllByUserId(Long userId) {
    return loanLedgerRepository.findAllByUser_UserId(UserId.of(userId));
  }
}
