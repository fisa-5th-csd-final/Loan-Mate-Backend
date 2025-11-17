package com.fisa.bank.loan.application.service;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.model.LoanDetail;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {
  private final LoanRepository loanRespository;
private final CoreBankingClient coreBankingClient;

  @Override
  @Transactional
  public List<LoanListResponse> getLoans(Long userId) {
    List<LoanListResponse> loanledgers =
        loanRespository.getLoans(UserId.of(userId)).stream().map(LoanListResponse::from).toList();
    // TODO: 위험도 추가
    return loanledgers;
  }

    @Override
    public LoanDetailResponse getLoanDetail(Long loanId) {
        String url = "/loans/ledger/" + loanId;
        LoanDetail loanDetail = coreBankingClient.fetchOne(url, LoanDetail.class);

        return LoanDetailResponse.from(loanId, loanDetail);
    }

}
