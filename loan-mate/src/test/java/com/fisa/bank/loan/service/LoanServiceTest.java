package com.fisa.bank.loan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.client.LoanAiClient;
import com.fisa.bank.loan.application.client.LoanCoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.model.LoanComment;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.loan.application.service.LoanService;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.persistence.loan.enums.LoanType;
import com.fisa.bank.persistence.loan.enums.RepaymentStatus;
import com.fisa.bank.persistence.loan.enums.RepaymentType;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

  @InjectMocks LoanService loanService;

  @Mock LoanRepository loanRepository;

  @Mock LoanCoreBankingClient loanCoreBankingClient;

  @Mock LoanAiClient loanAiClient;

  @Mock RequesterInfo requesterInfo;

  @Mock LoanReader loanReader;

  private static LoanDetail noRepaymentLoan; // 케이스 1: 상환 이력 없음
  private static LoanDetail terminatedLoan; // 케이스 2: 완료 상태 (스킵 대상)
  private static LoanDetail activeRepaymentLoan; // 케이스 3: 최소 1회 상환 (정상 계산 대상)
  private static LoanDetail fullyRepaidLoan; // 케이스 4: 만기 상환 직후

  private static final int TOTAL_TERM = 1;

  @BeforeEach
  void init() {
    when(loanAiClient.fetchLoanComment(anyLong())).thenReturn(new LoanComment(1L, ""));
  }

  @BeforeAll
  static void setup() {
    // 상환 시작일
    LocalDateTime createdAt = LocalDateTime.of(2025, 1, 15, 0, 0);
    // 가장 최근 상환일
    LocalDateTime lastRepaymentDate = LocalDateTime.of(2025, 5, 10, 0, 0);
    // 마지막 상환 완료일
    LocalDateTime fullRepaymentDate = LocalDateTime.of(2026, 1, 10, 0, 0);

    String accountNumber = "123478900000";
    // 1. 아직 상환한 적 없음 (paidMonth = 0)
    noRepaymentLoan =
        new LoanDetail(
            "미상환 대출",
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            accountNumber,
            LoanType.CREDIT,
            RepaymentType.BULLET,
            null,
            createdAt,
            TOTAL_TERM,
            RepaymentStatus.NORMAL,
            null,
            null);

    // 2. 상환 완료 상태 (스킵 대상)
    terminatedLoan =
        new LoanDetail(
            "중도 상환된 대출",
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            accountNumber,
            LoanType.CREDIT,
            RepaymentType.BULLET,
            fullRepaymentDate,
            createdAt,
            TOTAL_TERM,
            RepaymentStatus.TERMINATED,
            null,
            null);

    // 3. 만기 상환 직후 (12개월 / 12개월 = 100% 스킵 대상)
    fullyRepaidLoan =
        new LoanDetail(
            "만기 상환 직후 완료된 대출",
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            accountNumber,
            LoanType.CREDIT,
            RepaymentType.BULLET,
            fullRepaymentDate,
            createdAt,
            TOTAL_TERM,
            RepaymentStatus.COMPLETED,
            null,
            null);

    // 4. 최소 1번 상환 (4개월 / 12개월 = 33%)
    activeRepaymentLoan =
        new LoanDetail(
            "진행중인 대출",
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            BigDecimal.valueOf(1000),
            accountNumber,
            LoanType.CREDIT,
            RepaymentType.BULLET,
            lastRepaymentDate,
            createdAt,
            TOTAL_TERM,
            RepaymentStatus.NORMAL,
            null,
            null);
  }

  /** 1. 아직 상환한 적이 없는 경우 lastRepaymentDate가 null인 경우, paidMonth가 0L이 돼서 상환 진척률도 0이다. 0 / 12 = 0% */
  @Test
  @DisplayName("아직 상환한 적이 없는 경우 상환 진척률 0%이다.")
  void getLoanProgress_noRepayment_returnsZero() {
    // given
    when(loanReader.findLoanDetail(anyLong())).thenReturn(noRepaymentLoan);

    // when
    LoanDetailResponse loanDetailResponse = loanService.getLoanDetail(1L);

    // then
    assertEquals(0, loanDetailResponse.getProgress());
  }

  static Stream<LoanDetail> completedLoans() {
    return Stream.of(
        fullyRepaidLoan, // COMPLETED
        terminatedLoan // TERMINATED
        );
  }

  /** 2. 상환이 완료된 경우 RepaymentStatus가 COMPLETED 또는 TERMINATED인 경우 스킵되는지 확인한다. */
  @ParameterizedTest
  @MethodSource("completedLoans")
  @DisplayName("상환이 완료된 대출은 상환 진척률 100%이다.")
  void getLoanProgress_loanCompletedOrTerminated_isSkipped(LoanDetail loanDetail) {
    // given
    when(loanReader.findLoanDetail(anyLong())).thenReturn(loanDetail);

    // when
    LoanDetailResponse loanDetailResponse = loanService.getLoanDetail(1L);

    // then
    assertEquals(100, loanDetailResponse.getProgress());
  }

  /**
   * 3. 상환 최소 1번은 한 경우 (정상 계산 케이스) 1월 15일 생성 후 5월 10일에 마지막 상환을 한 경우 (4개월 경과) (4개월 * 100) / 12개월 =
   * 33.333... -> 반올림 (scale 0) 시 33%
   */
  @Test
  @DisplayName("상환은 최소 한 번 한 경우, 상환 진척률이 정상적으로 계산된다.")
  void getLoanProgress_repaymentMade_calculateCorrectly() {
    // given
    when(loanReader.findLoanDetail(anyLong())).thenReturn(activeRepaymentLoan);
    // 4 / 12 * 100 = 33%
    int expectedProgress = 33;
    // when
    LoanDetailResponse loanDetailResponse = loanService.getLoanDetail(1L);
    // then
    assertEquals(expectedProgress, loanDetailResponse.getProgress());
  }
}
