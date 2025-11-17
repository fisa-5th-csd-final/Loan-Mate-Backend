package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.persistence.loan.entity.LoanLedger;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReader {
  private final LoanRepository loanRespository;
  private final CoreBankingClient coreBankingClient;

  public LoanDetailResponse findLoanDetail(Long loanId) {
    String url = "/loans/ledger/" + loanId;
    LoanDetail loanDetail = coreBankingClient.fetchOne(url, LoanDetail.class);

    return LoanDetailResponse.from(loanId, loanDetail);
  }

  public LoanLedger findLoanLedgerById(Long loanLedgerId) {
    return loanRespository.getLoanLedger(loanLedgerId);
  }
}
