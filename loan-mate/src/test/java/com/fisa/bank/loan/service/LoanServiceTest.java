package com.fisa.bank.loan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fisa.bank.common.application.service.CoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanProgressResponse;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.service.LoanService;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

  @InjectMocks LoanService loanService;

  @Mock LoanRepository loanRepository;

  @Mock CoreBankingClient coreBankingClient;

  @Mock LoanReader loanReader;

  private Loan noRepaymentLoan; // 케이스 1: 상환 이력 없음
  private Loan completedLoan; // 케이스 2: 완료 상태 (스킵 대상)
  private Loan activeRepaymentLoan; // 케이스 3: 최소 1회 상환 (정상 계산 대상)
  private Loan fullyRepaidLoan; // 케이스 4: 만기 상환 직후

  private final int TOTAL_TERM = 12;

  @BeforeEach
  void setup() {
    // 상환 시작일
    LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 0, 0);
    // 가장 최근 상환일
    LocalDateTime lastRepaymentDate = LocalDateTime.of(2025, 5, 10, 0, 0);
    // 마지막 상환 완료일
    LocalDateTime fullRepaymentDate = LocalDateTime.of(2026, 1, 10, 0, 0);

    // 1. 아직 상환한 적 없음 (paidMonth = 0)
    noRepaymentLoan = new Loan(101L, "미상환 대출", createdAt, null, TOTAL_TERM, RepaymentStatus.NORMAL);

    // 2. 상환 완료 상태 (스킵 대상)
    completedLoan =
        new Loan(
            201L,
            "중도 상환된 대출",
            createdAt,
            fullRepaymentDate,
            TOTAL_TERM,
            RepaymentStatus.TERMINATED);

    // 3. 만기 상환 직후 (12개월 / 12개월 = 100% 스킵 대상)
    fullyRepaidLoan =
        new Loan(
            401L,
            "만기 상환 직후 완료된 대출",
            createdAt,
            fullRepaymentDate,
            TOTAL_TERM,
            RepaymentStatus.COMPLETED);

    // 4. 최소 1번 상환 (4개월 / 12개월 = 33%)
    activeRepaymentLoan =
        new Loan(301L, "진행중인 대출", createdAt, lastRepaymentDate, TOTAL_TERM, RepaymentStatus.NORMAL);
  }

  /** 1. 아직 상환한 적이 없는 경우 lastRepaymentDate가 null인 경우, paidMonth가 0L이 돼서 상환 진척률도 0이다. 0 / 12 = 0% */
  @Test
  @DisplayName("아직 상환한 적이 없는 경우 상환 진척률 0%이다.")
  void getLoanProgress_noRepayment_returnsZero() {
    // given
    when(loanReader.findLoans(anyLong())).thenReturn(Collections.singletonList(noRepaymentLoan));

    // when
    List<LoanProgressResponse> loanProgressResponses = loanService.getLoanProgress(anyLong());

    // then
    assertEquals(1, loanProgressResponses.size());
    assertEquals(BigDecimal.ZERO, loanProgressResponses.get(0).getProgress());
  }

  /** 2. 상환이 완료된 경우 RepaymentStatus가 COMPLETED 또는 TERMINATED인 경우 스킵되는지 확인한다. */
  @Test
  @DisplayName("상환이 완료된 대출은 상환 진척률을 계산하지 않는다.")
  void getLoanProgress_loanCompletedOrTerminated_isSkipped() {
    // given
    when(loanReader.findLoans(anyLong())).thenReturn(List.of(fullyRepaidLoan, completedLoan));
    // when
    List<LoanProgressResponse> loanProgressResponses = loanService.getLoanProgress(anyLong());

    // then
    assertEquals(0, loanProgressResponses.size());
  }

  /**
   * 3. 상환 최소 1번은 한 경우 (정상 계산 케이스) 1월 15일 생성 후 5월 10일에 마지막 상환을 한 경우 (4개월 경과) (4개월 * 100) / 12개월 =
   * 33.333... -> 반올림 (scale 0) 시 33%
   */
  @Test
  @DisplayName("상환은 최소 한 번 한 경우, 상환 진척률이 정상적으로 계산된다.")
  void getLoanProgress_repaymentMade_calculateCorrectly() {
    // given
    when(loanReader.findLoans(anyLong()))
        .thenReturn(Collections.singletonList(activeRepaymentLoan));
    // 4 / 12 * 100 = 33%
    BigDecimal expectedProgress = new BigDecimal(33);
    // when
    List<LoanProgressResponse> loanProgressResponses = loanService.getLoanProgress(anyLong());
    // then
    assertEquals(expectedProgress, loanProgressResponses.get(0).getProgress());
  }
}
