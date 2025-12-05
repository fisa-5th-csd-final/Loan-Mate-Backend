package com.fisa.bank.loan.application.service.reader;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.calculator.CalculatorService;
import com.fisa.bank.loan.application.client.LoanCoreBankingClient;
import com.fisa.bank.loan.application.exception.LoanLedgerNotFoundException;
import com.fisa.bank.loan.application.model.Loan;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.model.PrepaymentInfo;
import com.fisa.bank.loan.application.repository.LoanRepository;
import com.fisa.bank.model.MonthlyRepayment;
import com.fisa.bank.persistence.common.id.BaseId;
import com.fisa.bank.persistence.loan.entity.LoanLedger;
import com.fisa.bank.persistence.loan.repository.LoanLedgerRepository;
import com.fisa.bank.persistence.user.entity.User;
import com.fisa.bank.persistence.user.entity.id.UserId;
import com.fisa.bank.user.application.model.ServiceUser;
import com.fisa.bank.user.application.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReader {
  private final LoanRepository loanRepository;
  private final LoanLedgerRepository loanLedgerRepository;
  private final LoanCoreBankingClient loanCoreBankingClient;
  private final UserRepository userRepository;
  private final CalculatorService calculatorService;

  public LoanDetail findLoanDetail(Long loanId) { // 특정 LoanLedger의 정보
    return loanCoreBankingClient.fetchLoanDetail(loanId);
  }

  public List<LoanDetail> findLoanDetails() {
    return loanCoreBankingClient.fetchLoanDetails();
  }

  public List<Loan> findLoans(Long userId) {
    return loanRepository.getLoans(UserId.of(userId));
  }

  public Loan findLoanById(Long loanId) {
    return loanRepository
        .findById(loanId)
        .orElseThrow(() -> new LoanLedgerNotFoundException(loanId));
  }

  public List<PrepaymentInfo> findPrepaymentInfos() {
    return loanCoreBankingClient.fetchPrepaymentInfos();
  }

  public List<LoanLedger> findAllByUserId(Long userId) {
    return loanLedgerRepository.findAllByUser_UserId(UserId.of(userId));
  }

  public BigDecimal calculatePeerAverageRepaymentRatio(LocalDate birthday, int ageRangeYears) {
    if (birthday == null) {
      return BigDecimal.ZERO;
    }

    LocalDate start = birthday.minusYears(ageRangeYears);
    LocalDate end = birthday.plusYears(ageRangeYears);

    List<LoanLedger> loanLedgers = loanLedgerRepository.findAll();

    List<BigDecimal> ratios =
        loanLedgers.stream()
            .map(ledger -> toRepaymentRatioIfInRange(ledger, start, end))
            .filter(Objects::nonNull)
            .toList();

    if (ratios.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal total = ratios.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return total.divide(BigDecimal.valueOf(ratios.size()), 4, RoundingMode.HALF_UP);
  }

  private BigDecimal toRepaymentRatioIfInRange(
      LoanLedger loanLedger, LocalDate start, LocalDate end) {

    if (loanLedger == null || loanLedger.getUser() == null) {
      return null;
    }

    Long serviceUserId =
        Optional.ofNullable(loanLedger.getUser())
            .map(User::getUserId)
            .map(BaseId::getValue)
            .orElse(null);

    ServiceUser serviceUser =
        serviceUserId != null ? userRepository.findById(serviceUserId).orElse(null) : null;

    LocalDate birth = serviceUser != null ? serviceUser.getBirthday() : null;

    if (birth == null || birth.isBefore(start) || birth.isAfter(end)) {
      return null;
    }

    BigDecimal monthlyRepayment =
        Optional.ofNullable(calculatorService.calculate(loanLedger))
            .map(this::firstMonthlyRepaymentAmount)
            .orElse(null);
    BigDecimal income = serviceUser != null ? serviceUser.getIncome() : null;

    if (monthlyRepayment == null || income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }

    return monthlyRepayment.divide(income, 4, RoundingMode.HALF_UP);
  }

  private BigDecimal firstMonthlyRepaymentAmount(List<MonthlyRepayment> repayments) {
    return repayments.stream().findFirst().map(MonthlyRepayment::getMonthlyPayment).orElse(null);
  }
}
