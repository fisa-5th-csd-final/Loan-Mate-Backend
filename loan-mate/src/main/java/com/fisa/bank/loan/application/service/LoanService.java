package com.fisa.bank.loan.application.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;
import com.fisa.bank.persistence.user.entity.id.UserId;

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

  @Override
  @Transactional
  public List<LoanProgressResponse> getLoanProgress(Long userId) {
    List<LoanProgressResponse> progressList = new ArrayList<>();

    // TODO: 유저 id로 대출 리스트를 조회하고,
    List<Loan> loanledgers = loanRespository.getLoans(UserId.of(userId));

    // TODO: 각 대출 리스트 순회하면서 ID를 가지고 디테일을 조회
    for (Loan loan : loanledgers) {
      Long id = loan.getLoanId();
      LoanLedger loanLedger = loanReader.findLoanLedgerById(id);

      // 이미 상환 완료된 대출은 스킵
      if (loanLedger.getRepaymentStatus().equals(RepaymentStatus.COMPLETED)
          || loanLedger.getRepaymentStatus().equals(RepaymentStatus.TERMINATED)) {
        continue;
      }

      // TODO: 상환 진척률 계산
      // 현재 납부한 개월 수 -> loan_end_date - last_repayment_date = 개월
      LocalDateTime loanEndDate = loanLedger.getLoanEndDate();
      LocalDateTime lastRepaymentDate = loanLedger.getLastRepaymentDate();
      long paidMonth = 0;

      if (lastRepaymentDate != null) {
        paidMonth =
            ChronoUnit.MONTHS.between(
                lastRepaymentDate.withDayOfMonth(1), loanEndDate.withDayOfMonth(1));
      }

      // 총 개월 수 -> 원장성 테이블에서 조회
      int totalTerm = loanLedger.getTerm();

      // 상환 진척률 계산
      BigDecimal progress =
          BigDecimal.valueOf(paidMonth)
              .divide(BigDecimal.valueOf(totalTerm), 0, RoundingMode.HALF_UP)
              .multiply(BigDecimal.valueOf(100));

      LoanProgressResponse loanProgress =
          LoanProgressResponse.from(id, loanLedger.getLoanProduct().getName(), progress);
      progressList.add(loanProgress);
    }
    return progressList;
  }
}
