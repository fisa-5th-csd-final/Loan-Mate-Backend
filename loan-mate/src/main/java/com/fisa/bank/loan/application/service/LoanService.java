package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.loan.application.dto.response.LoanAutoDepositResponse;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.dto.response.LoansWithPrepaymentBenefitResponse;
import com.fisa.bank.loan.application.model.InterestDetail;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;

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

    Loan loan = loanReader.findLoanById(loanId);

    return LoanAutoDepositResponse.from(loan);
  }

  @Override
  public void cancelLoan(Long loanId) {
    String url = "/loans/" + loanId;
    coreBankingClient.fetchOneDelete(url);
  }

  public List<LoansWithPrepaymentBenefitResponse> getLoansWithPrepaymentBenefit() {
    List<LoansWithPrepaymentBenefitResponse> loansWithPrepaymentBenefitResponses =
        new ArrayList<>();

    List<PrepaymentInfo> prepaymentInfos = loanReader.findPrepaymentInfos();

    for (PrepaymentInfo prepaymentInfo : prepaymentInfos) {
      List<InterestDetail> interestDetails = prepaymentInfo.getInterestDetailResponses();
      BigDecimal earlyRepayment = prepaymentInfo.getEarlyRepayment();

      BigDecimal remainInterests =
          interestDetails.stream()
              .map(InterestDetail::getInterest)
              .reduce(BigDecimal.ZERO, BigDecimal::add); // 초기값, 합산 연산

      // 선납 이득인지 확인 - 중도 상환 수수료가 남은 이자 합보다 더 적다면 리스트에 추가
      if (earlyRepayment.compareTo(remainInterests) < 0) {
        BigDecimal benefit = remainInterests.subtract(earlyRepayment);
        loansWithPrepaymentBenefitResponses.add(
            LoansWithPrepaymentBenefitResponse.from(prepaymentInfo.getLoanProductName(), benefit));
      }
    }
    return loansWithPrepaymentBenefitResponses;
  }

  @Override
  public void updateAutoDepositEnabled(Long loanId, boolean autoDepositEnabled) {

    String endpoint = "/loans/" + loanId + "/auto-deposit";
    AutoDepositUpdateRequest body = new AutoDepositUpdateRequest(autoDepositEnabled);

    coreBankingClient.patch(endpoint, body);
  }
}
