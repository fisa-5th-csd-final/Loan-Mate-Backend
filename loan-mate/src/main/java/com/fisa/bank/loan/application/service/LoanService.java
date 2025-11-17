package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {
  private final LoanRepository loanRepository;
  private final CoreBankingClient coreBankingClient;
  private static final String LOAN_LEDGER_ENDPOINT_PREFIX = "/loans/ledger/";

  @Override
  @Transactional
  public List<LoanListResponse> getLoans(Long userId) {
    List<LoanListResponse> loanledgers =
        loanRepository.getLoans(UserId.of(userId)).stream().map(LoanListResponse::from).toList();
    // TODO: 위험도 추가
    return loanledgers;
  }

  @Override
  public LoanDetailResponse getLoanDetail(Long loanId) {
    LoanDetail loanDetail =
        coreBankingClient.fetchOne(LOAN_LEDGER_ENDPOINT_PREFIX + loanId, LoanDetail.class);

    return LoanDetailResponse.from(loanId, loanDetail);
  }
}
