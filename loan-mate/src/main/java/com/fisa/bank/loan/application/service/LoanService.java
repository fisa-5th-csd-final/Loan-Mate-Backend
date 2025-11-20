package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanAutoDepositResponse;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.loan.entity.LoanLedger;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {

  private final LoanReader loanReader;
  private final CoreBankingClient coreBankingClient;

  @Override
  @Transactional
  public List<LoanListResponse> getLoans(Long userId) {
    List<LoanListResponse> loanLedgers =
        loanReader.findLoans(userId).stream().map(LoanListResponse::from).toList();
    // TODO: 위험도 추가
    return loanLedgers;
  }

  @Override
  public LoanDetailResponse getLoanDetail(Long loanId) {
    LoanDetail loanDetail = loanReader.findLoanDetail(loanId);
    Integer progress = calculateProgressRate(loanDetail).intValueExact();

    loanDetail.setProgress(progress);
    return LoanDetailResponse.from(loanId, loanDetail);
  }

  private BigDecimal calculateProgressRate(LoanDetail loan) {

    // 1. 현재 납부한 개월 수 계산
    LocalDateTime lastRepaymentDate = loan.getLastRepaymentDate();

    long paidMonth =
        Optional.ofNullable(lastRepaymentDate)
            .map(
                d ->
                    ChronoUnit.MONTHS.between(
                        loan.getCreatedAt().toLocalDate().withDayOfMonth(1),
                        d.toLocalDate().withDayOfMonth(1)))
            .orElse(0L);

    // 2. 총 개월 수 조회
    int totalTerm = loan.getTerm() * 12;

    // 3. 상환 진척률 계산 (paidMonth / totalTerm * 100)
    // Scale 0으로 설정하여 항상 정수 퍼센트(예: 33)로 반올림하여 반환합니다.
    BigDecimal progressRate =
        BigDecimal.valueOf(paidMonth)
            .multiply(BigDecimal.valueOf(100))
            .divide(BigDecimal.valueOf(totalTerm), 0, RoundingMode.HALF_UP);

    return progressRate;
  }

  @Override
  @Transactional(readOnly = true)
  public LoanAutoDepositResponse getAutoDeposit(Long loanId) {

    LoanLedger loanLedger = loanReader.findLoanLedgerById(loanId);

    return LoanAutoDepositResponse.from(loanLedger);
  }

  @Override
  @Transactional
  public void cancelLoan(Long loanId) {
    String url = "/loans/" + loanId;
    coreBankingClient.fetchOneDelete(url);
  }
}
