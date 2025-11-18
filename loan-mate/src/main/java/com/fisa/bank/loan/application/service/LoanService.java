package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.dto.response.LoanProgressResponse;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanService implements ManageLoanUseCase {
  private final LoanRepository loanRepository;
  private final CoreBankingClient coreBankingClient;
  private final LoanReader loanReader;
  private static final String LOAN_LEDGER_ENDPOINT_PREFIX = "/loans/ledger/";

  @Override
  @Transactional
  public List<LoanListResponse> getLoans(Long userId) {
    List<LoanListResponse> loanledgers =
        loanReader.findLoans(userId).stream().map(LoanListResponse::from).toList();
    // TODO: 위험도 추가
    return loanledgers;
  }

  @Override
  public LoanDetailResponse getLoanDetail(Long loanId) {
    LoanDetail loanDetail =
        coreBankingClient.fetchOne(LOAN_LEDGER_ENDPOINT_PREFIX + loanId, LoanDetail.class);

    return LoanDetailResponse.from(loanId, loanDetail);
  }

  @Override
  @Transactional(readOnly = true)
  public List<LoanProgressResponse> getLoanProgress(Long userId) {
    List<LoanProgressResponse> progressList = new ArrayList<>();
    EnumSet<RepaymentStatus> skipped =
        EnumSet.of(RepaymentStatus.COMPLETED, RepaymentStatus.TERMINATED);

    List<Loan> loans = loanReader.findLoans(userId);

    for (Loan loan : loans) {

      // 이미 상환 완료된 대출은 스킵
      if (skipped.contains(loan.getRepaymentStatus())) {
        continue;
      }

      // 상환 진척률 계산 로직
      // 현재 납부한 개월 수 -> last_repayment_date(마지막 상환일) - createdAt(생성 날짜) = 개월
      LocalDateTime lastRepaymentDate = loan.getLastRepaymentDate();

      long paidMonth =
          Optional.ofNullable(lastRepaymentDate)
              .map(
                  d ->
                      ChronoUnit.MONTHS.between(
                          loan.getCreatedAt().withDayOfMonth(1), d.withDayOfMonth(1)))
              .orElse(0L);

      // 총 개월 수 -> 원장성 테이블에서 조회
      int totalTerm = loan.getTerm();

      // 상환 진척률 계산
      BigDecimal progress =
          BigDecimal.valueOf(paidMonth)
              .divide(BigDecimal.valueOf(totalTerm), 0, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));

      LoanProgressResponse loanProgress =
          LoanProgressResponse.from(loan.getLoanId(), loan.getLoanName(), progress);
      progressList.add(loanProgress);
    }
    return progressList;
  }
}
