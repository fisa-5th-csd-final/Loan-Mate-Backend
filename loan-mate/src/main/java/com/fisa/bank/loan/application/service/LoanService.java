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

import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.client.LoanAiClient;
import com.fisa.bank.loan.application.client.LoanCoreBankingClient;
import com.fisa.bank.loan.application.dto.request.AiSimulationRequest;
import com.fisa.bank.loan.application.dto.response.*;
import com.fisa.bank.loan.application.dto.response.AutoDepositResponse;
import com.fisa.bank.loan.application.dto.response.LoanAiCommentResponse;
import com.fisa.bank.loan.application.dto.response.LoanAutoDepositResponse;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.dto.response.LoanRiskResponse;
import com.fisa.bank.loan.application.dto.response.LoansWithPrepaymentBenefitResponse;
import com.fisa.bank.loan.application.model.*;
import com.fisa.bank.loan.application.model.InterestDetail;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.LoanRiskDetail;
import com.fisa.bank.loan.application.model.LoanRisks;
import com.fisa.bank.loan.application.model.PrepaymentInfo;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.loan.persistence.enums.RiskLevel;
import com.fisa.bank.persistence.loan.entity.LoanLedger;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {

  private final LoanReader loanReader;
  private final LoanCoreBankingClient loanCoreBankingClient;
  private final LoanAiClient loanAiClient;
  private final RequesterInfo requesterInfo;

  @Override
  @Transactional
  // TODO: 캐시 추가
  public List<LoanListResponse> getLoans() {
    Long userId = requesterInfo.getCoreBankingUserId();
    // db에서 대출 리스트 조회
    List<Loan> loans = loanReader.findLoans(userId);

    // 위험도 fetch
    LoanRisks loanRisks = loanAiClient.fetchLoanRisks(userId);

    // 위험도와 대출 id를 매핑하는 맵
    Map<Long, BigDecimal> riskMap =
        loanRisks.getLoans().stream()
            .collect(Collectors.toMap(LoanRiskDetail::getLoanLedgerId, LoanRiskDetail::getRisk));

    // 대출 리스트와 위험도 매핑
    return loans.stream().map(loan -> LoanListResponse.from(loan, riskMap)).toList();
  }

  @Override
  public LoanAiCommentResponse getAiComment(Long loanId) {
    Long userId = requesterInfo.getCoreBankingUserId();
    LoanRisks loanRisks = loanAiClient.fetchLoanRisks(userId);

    return Optional.ofNullable(loanRisks).map(LoanRisks::getLoans).orElse(List.of()).stream()
        .filter(detail -> loanId.equals(detail.getLoanLedgerId()))
        .map(detail -> new LoanAiCommentResponse(detail.getLoanLedgerId(), detail.getExplanation()))
        .findFirst()
        .orElse(new LoanAiCommentResponse(loanId, null));
  }

  @Override
  public LoanRiskResponse getLoanRisk() {
    Long userId = requesterInfo.getCoreBankingUserId();
    LoanRisks loanRisks = loanAiClient.fetchLoanRisks(userId);

    return Optional.ofNullable(loanRisks)
        .map(LoanRisks::getOverallRisk)
        .map(risk -> new LoanRiskResponse(risk, RiskLevel.fromRiskScore(risk)))
        .orElse(new LoanRiskResponse(null, null));
  }

  @Override
  // TODO: 캐시 추가
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
    loanCoreBankingClient.cancelLoan(loanId);
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
            LoansWithPrepaymentBenefitResponse.from(
                prepaymentInfo.getLoanLedgerId(), prepaymentInfo.getBalance(), prepaymentInfo.getLoanProductName(), benefit, prepaymentInfo.getMustPaidAmount(), prepaymentInfo.getAccountNumber()));
      }
    }
    return loansWithPrepaymentBenefitResponses;
  }

  @Override
  public void updateAutoDepositEnabled(Long loanId, boolean autoDepositEnabled) {

    loanCoreBankingClient.updateAutoDeposit(loanId, autoDepositEnabled);
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
                    .loanLedgerId(ledger.getLoanLedgerId())
                    .loanName(
                        ledger.getLoanProduct() != null ? ledger.getLoanProduct().getName() : null)
                    .accountBalance(
                        ledger.getAccount() != null ? ledger.getAccount().getBalance() : null)
                    .autoDepositEnabled(ledger.isAutoDepositEnabled())
                    .build())
        .toList();
  }

  @Override
  public List<LoanDetailResponse> getLoanDetails() {
    List<LoanDetail> loanDetails = loanReader.findLoanDetails();

    List<LoanDetailResponse> responseList =
        loanDetails.stream()
            .map(
                loanDetail -> {
                  // progress 계산
                  loanDetail.setProgress(calculateProgressRate(loanDetail).intValueExact());
                  // DTO 변환
                  return LoanDetailResponse.from(loanDetail.getLoanLedgerId(), loanDetail);
                })
            .toList();
    return responseList;
  }

  @Override
  public AiSimulationResponse processAiSimulation(AiSimulationRequest request) {
    request.setUser_id(requesterInfo.getCoreBankingUserId());
    return loanAiClient.processAiSimulation(request);
  }
}
