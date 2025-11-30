package com.fisa.bank.account.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.account.application.exception.SalaryAccountNotFoundException;
import com.fisa.bank.account.application.model.RecommendedSpending;
import com.fisa.bank.account.application.repository.AccountRepository;
import com.fisa.bank.account.application.usecase.GetRecommendedSpendingUseCase;
import com.fisa.bank.account.application.util.SpendingRatioLoader;
import com.fisa.bank.account.persistence.repository.JpaAccountTransactionRepository;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;
import com.fisa.bank.persistence.user.entity.User;
import com.fisa.bank.persistence.user.entity.id.UserId;
import com.fisa.bank.persistence.user.repository.UserRepository;
import com.fisa.bank.user.application.exception.ServiceUserNotFoundException;
import com.fisa.bank.user.application.model.ServiceUser;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedSpendingService implements GetRecommendedSpendingUseCase {

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal BUDGET_RATIO = BigDecimal.valueOf(0.4);

  private final RequesterInfo requesterInfo;
  private final UserRepository userRepository; // CoreBanking User
  private final com.fisa.bank.user.application.repository.UserRepository
      serviceUserRepository; // service User

  private final AccountRepository accountRepository;
  private final JpaAccountTransactionRepository accountTransactionRepository;
  private final ManualLedgerRepository manualLedgerRepository;
  private final LoanReader loanReader;

  private final SpendingRatioLoader ratioLoader;

  @Override
  public RecommendedSpending execute(int year, int month) {

    validateMonth(month);

    YearMonth currentMonth = YearMonth.of(year, month);
    YearMonth previousMonth = currentMonth.minusMonths(1);

    LocalDateTime salaryStart = previousMonth.atDay(1).atStartOfDay();
    LocalDateTime salaryEnd = previousMonth.plusMonths(1).atDay(1).atStartOfDay();

    LocalDateTime manualStart = currentMonth.atDay(1).atStartOfDay();
    LocalDateTime manualEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

    // CoreBanking User → 급여 통장 조회
    User coreUser = getCoreBankingUser();

    Account salaryAccount = findSalaryAccount(coreUser);
    AccountId accountId = salaryAccount.getAccountId();

    BigDecimal salaryIncome =
        safe(accountTransactionRepository.sumMonthlyIncome(accountId, salaryStart, salaryEnd));

    // ServiceUser → birthday 조회 → 연령대 계산
    ServiceUser serviceUser = getServiceUser();

    BigDecimal manualIncome =
        safe(
            manualLedgerRepository.sumAmountByUserIdAndTypeBetween(
                serviceUser.getUserId(),
                ManualLedgerType.INCOME,
                manualStart.toLocalDate(),
                manualEnd.toLocalDate()));

    BigDecimal monthlyRepayment = sumMonthlyRepayment();

    BigDecimal availableIncome =
        salaryIncome.add(manualIncome).subtract(monthlyRepayment).max(ZERO);

    BigDecimal variableSpendingBudget =
        availableIncome.multiply(BUDGET_RATIO).setScale(0, RoundingMode.DOWN);

    // 연령대 별 추천 카테고리 비율 적용
    Map<ConsumptionCategory, BigDecimal> categoryRecommendation =
        buildCategoryRecommendation(variableSpendingBudget, serviceUser);

    return new RecommendedSpending(variableSpendingBudget, categoryRecommendation);
  }

  private void validateMonth(int month) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("월은 1~12 사이여야 합니다.");
    }
  }

  // CoreBankingUser 조회
  private User getCoreBankingUser() {
    Long coreUserId = requesterInfo.getCoreBankingUserId();
    return userRepository
        .findById(UserId.of(coreUserId))
        .orElseThrow(ServiceUserNotFoundException::new);
  }

  // ServiceUser 조회
  private ServiceUser getServiceUser() {
    Long serviceUserId = requesterInfo.getServiceUserId();
    return serviceUserRepository
        .findById(serviceUserId)
        .orElseThrow(ServiceUserNotFoundException::new);
  }

  // 급여 계좌 조회
  private Account findSalaryAccount(User coreUser) {
    return accountRepository
        .findSalaryAccount(coreUser)
        .orElseThrow(() -> new SalaryAccountNotFoundException(coreUser.getUserId().getValue()));
  }

  // 월 상환금
  private BigDecimal sumMonthlyRepayment() {
    List<LoanDetail> loans = loanReader.findLoanDetails();
    return loans.stream()
        .map(LoanDetail::getMonthlyRepayment)
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }

  private BigDecimal safe(BigDecimal v) {
    return v == null ? ZERO : v;
  }

  // 연령대 기반 추천 금액
  private Map<ConsumptionCategory, BigDecimal> buildCategoryRecommendation(
      BigDecimal variableBudget, ServiceUser user) {

    var ratios = ratioLoader.getRatios(user.getBirthday()); // birthday → 연령대 자동 판별

    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);
    BigDecimal totalAllocated = ZERO;

    for (var entry : ratios.entrySet()) {
      BigDecimal allocated =
          variableBudget.multiply(entry.getValue()).setScale(0, RoundingMode.DOWN);

      result.put(entry.getKey(), allocated);
      totalAllocated = totalAllocated.add(allocated);
    }

    BigDecimal remainder = variableBudget.subtract(totalAllocated);
    if (remainder.compareTo(ZERO) > 0) {
      result.computeIfPresent(ConsumptionCategory.ENTERTAINMENT, (k, v) -> v.add(remainder));
    }

    for (ConsumptionCategory category : ConsumptionCategory.values()) {
      result.putIfAbsent(category, ZERO);
    }

    return result;
  }
}
