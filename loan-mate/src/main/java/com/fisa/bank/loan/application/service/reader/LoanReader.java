package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.exception.LoanLedgerNotFoundException;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanAutoDeposit;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReader {
  private final LoanRepository loanRepository;
  private final CoreBankingClient coreBankingClient;

  private static final String PREPAYMENT_INFOS_URL = "/loans/prepayment-infos";
  private static final String LOAN_DETAILS_URL = "/loans/ledgers/details";

  public LoanDetail findLoanDetail(Long loanId) {
    String url = "/loans/ledger/" + loanId;

    return coreBankingClient.fetchOne(url, LoanDetail.class);
  }

  public List<LoanDetail> findLoanDetails() {
    return coreBankingClient.fetchList(LOAN_DETAILS_URL, LoanDetail.class);
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

  public List<LoanAutoDeposit> findAutoDepositByUserId(Long userId) {
    return loanRepository.findAutoDepositByUserId(userId);
  }
}
