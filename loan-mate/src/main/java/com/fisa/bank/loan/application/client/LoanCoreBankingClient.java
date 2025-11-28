package com.fisa.bank.loan.application.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fisa.bank.common.application.util.core_bank.CoreBankingClient;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanCoreBankingClient {

  private static final String LOANS_BASE_PATH = "/loans";

  private final CoreBankingClient coreBankingClient;

  public LoanDetail fetchLoanDetail(Long loanId) {
    return coreBankingClient.fetchOne(LOANS_BASE_PATH + "/ledger/" + loanId, LoanDetail.class);
  }

  public List<PrepaymentInfo> fetchPrepaymentInfos() {
    return coreBankingClient.fetchList(
        LOANS_BASE_PATH + "/prepayment-infos", PrepaymentInfo.class);
  }

  public void cancelLoan(Long loanId) {
    coreBankingClient.delete(LOANS_BASE_PATH + "/" + loanId);
  }

  public void updateAutoDeposit(Long loanId, boolean autoDepositEnabled) {
    coreBankingClient.patch(
        LOANS_BASE_PATH + "/" + loanId + "/auto-deposit",
        new AutoDepositUpdateRequest(autoDepositEnabled));
  }
}
