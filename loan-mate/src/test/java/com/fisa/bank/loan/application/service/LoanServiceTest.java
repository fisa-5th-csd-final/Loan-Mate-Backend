package com.fisa.bank.loan.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fisa.bank.calculator.CalculatorService;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.client.LoanAiClient;
import com.fisa.bank.loan.application.client.LoanCoreBankingClient;
import com.fisa.bank.loan.application.dto.response.LoanRepaymentRatioResponse;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.model.MonthlyRepayment;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.user.application.model.CreditRating;
import com.fisa.bank.user.application.model.CustomerLevel;
import com.fisa.bank.user.application.model.ServiceUser;
import com.fisa.bank.user.application.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

  @Mock private LoanReader loanReader;
  @Mock private LoanCoreBankingClient loanCoreBankingClient;
  @Mock private LoanAiClient loanAiClient;
  @Mock private RequesterInfo requesterInfo;
  @Mock private UserRepository userRepository;
  @Mock private CalculatorService calculatorService;

  private LoanService loanService;

  @BeforeEach
  void setUp() {
    loanService =
        new LoanService(
            loanReader,
            loanCoreBankingClient,
            loanAiClient,
            requesterInfo,
            userRepository,
            calculatorService);
  }

  @Test
  void calculatesUserAndPeerRepaymentRatio() {
    // given
    LocalDate birthday = LocalDate.of(1990, 1, 1);
    BigDecimal income = new BigDecimal("2000.00");

    when(requesterInfo.getServiceUserId()).thenReturn(1L);
    when(requesterInfo.getCoreBankingUserId()).thenReturn(1L);
    when(userRepository.findById(1L))
        .thenReturn(
            java.util.Optional.of(
                new ServiceUser(
                    1L,
                    "name",
                    "address",
                    "job",
                    birthday,
                    CreditRating.A,
                    CustomerLevel.VIP,
                    income)));

    LoanLedger loanLedger1 = mock(LoanLedger.class);
    LoanLedger loanLedger2 = mock(LoanLedger.class);

    when(loanReader.findAllByUserId(1L)).thenReturn(List.of(loanLedger1, loanLedger2));
    LocalDateTime fixedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0);

    when(calculatorService.calculate(loanLedger1))
        .thenReturn(
            List.of(
                new MonthlyRepayment(
                    1,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    new BigDecimal("100"),
                    BigDecimal.ZERO,
                    fixedDateTime)));
    when(calculatorService.calculate(loanLedger2))
        .thenReturn(
            List.of(
                new MonthlyRepayment(
                    1,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    new BigDecimal("300"),
                    BigDecimal.ZERO,
                    fixedDateTime)));
    when(loanReader.calculatePeerAverageRepaymentRatio(eq(birthday), eq(5)))
        .thenReturn(new BigDecimal("0.3000"));

    // when
    LoanRepaymentRatioResponse response = loanService.getRepaymentIncomeRatio();

    // then
    assertThat(response.monthlyIncome()).isEqualByComparingTo(income);
    assertThat(response.totalMonthlyRepayment()).isEqualByComparingTo("400");
    assertThat(response.ratio()).isEqualByComparingTo("0.2000");
    assertThat(response.peerAverageRatio()).isEqualByComparingTo("0.3000");
  }
}
