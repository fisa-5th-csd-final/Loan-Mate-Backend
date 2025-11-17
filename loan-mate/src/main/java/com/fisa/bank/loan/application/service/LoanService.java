package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.repository.LoanRespository;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.user.entity.id.UserId;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {
  private final LoanRespository loanRespository;

  @Override
  @Transactional
  public List<LoanListResponse> getLoans(Long userId) {
    List<LoanListResponse> loanledgers =
        loanRespository.getLoans(UserId.of(userId)).stream().map(LoanListResponse::from).toList();
    // TODO: 위험도 추가
    return loanledgers;
  }
}
