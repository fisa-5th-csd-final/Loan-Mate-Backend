package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.AiClient;
import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.loan.application.dto.response.*;
import com.fisa.bank.loan.application.model.*;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.loan.entity.LoanLedger;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {

  private final LoanReader loanReader;
  private final CoreBankingClient coreBankingClient;
  private final RequesterInfo requesterInfo;
  private static final String PREDICT_URL = "/predict";
  private static final String LOAN_COMMENT = "/insight/loan";
  private final AiClient aiClient;
  private static final String AI_REQUEST_KEY_USER_ID = "user_id";
  private static final String AI_REQUEST_KEY_LOAN_LEDGER_ID = "loan_ledger_id";

  @Override
  @Transactional
  public List<LoanListResponse> getLoans() {
    Long userId = requesterInfo.getCoreBankingUserId();
    // db에서 대출 리스트 조회
    List<Loan> loans = loanReader.findLoans(userId);

    // 위험도 fetch
    LoanRisks loanRisks =
        aiClient.fetchOne(PREDICT_URL, Map.of(AI_REQUEST_KEY_USER_ID, userId), LoanRisks.class);

    // 위험도와 대출 id를 매핑하는 맵
    Map<Long, BigDecimal> riskMap =
        loanRisks.getLoans().stream()
            .collect(Collectors.toMap(LoanRiskDetail::getLoanLedgerId, LoanRiskDetail::getRisk));

    // 대출 리스트와 위험도 매핑
    return loans.stream().map(loan -> LoanListResponse.from(loan, riskMap)).toList();
  }

  @Override
  public LoanDetailResponse getLoanDetail(Long loanId) {
    LoanDetail loanDetail = loanReader.findLoanDetail(loanId);
    Integer progress = calculateProgressRate(loanDetail).intValueExact();

    loanDetail.setProgress(progress);
    // 대출 LLM 코멘트
    LoanComment loanComment =
        aiClient.fetchOne(
            LOAN_COMMENT, Map.of(AI_REQUEST_KEY_LOAN_LEDGER_ID, loanId), LoanComment.class);

    if (!loanComment.getLoanLedgerId().equals(loanId)) {
      throw new IllegalStateException("요청한 loanId와 응답 loanLedgerId가 불일치합니다.");
    }

    loanDetail.setComment(loanComment.getComment());
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

  @Override
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

  @Transactional(readOnly = true)
  public List<AutoDepositResponse> getAutoDepositSummary() {
    Long userId = requesterInfo.getCoreBankingUserId();
    // UserReader 또는 Reader 계층을 통해 대출 조회
    // (LoanLedgerRepository 직접 사용 X — 기존 서비스 일관성 유지)
    List<LoanLedger> loanLedgers = loanReader.findAllByUserId(userId);

    return loanLedgers.stream()
        .map(
            ledger ->
                AutoDepositResponse.builder()
                    .loanName(ledger.getLoanProduct().getName())
                    .accountBalance(
                        ledger.getAccount() != null ? ledger.getAccount().getBalance() : null)
                    .autoDepositEnabled(ledger.isAutoDepositEnabled())
                    .build())
        .toList();
  }
}
