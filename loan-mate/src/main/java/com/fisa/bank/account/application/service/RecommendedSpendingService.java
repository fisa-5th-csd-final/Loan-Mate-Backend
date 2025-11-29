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
import com.fisa.bank.account.application.usecase.GetRecommendedSpendingUseCase;
import com.fisa.bank.account.persistence.repository.JpaAccountTransactionRepository;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.loan.application.model.LoanDetail;
import com.fisa.bank.loan.application.service.reader.LoanReader;
import com.fisa.bank.persistence.account.entity.Account;
import com.fisa.bank.persistence.account.entity.id.AccountId;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;
import com.fisa.bank.persistence.account.repository.AccountRepository;
import com.fisa.bank.persistence.user.entity.User;
import com.fisa.bank.persistence.user.entity.id.UserId;
import com.fisa.bank.persistence.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendedSpendingService implements GetRecommendedSpendingUseCase {

  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private static final BigDecimal BUDGET_RATIO = BigDecimal.valueOf(0.4);
  private static final BigDecimal FOOD_RATIO = BigDecimal.valueOf(0.4);
  private static final BigDecimal TRANSPORT_RATIO = BigDecimal.valueOf(0.15);
  private static final BigDecimal SHOPPING_RATIO = BigDecimal.valueOf(0.25);
  private static final BigDecimal ENTERTAINMENT_RATIO = BigDecimal.valueOf(0.2);

  private final RequesterInfo requesterInfo;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final JpaAccountTransactionRepository accountTransactionRepository;
  private final ManualLedgerRepository manualLedgerRepository;
  private final LoanReader loanReader;

  @Override
  public RecommendedSpending execute(int year, int month) {

    validateMonth(month);

    YearMonth yearMonth = YearMonth.of(year, month);
    YearMonth previousMonth = yearMonth.minusMonths(1);

    LocalDateTime salaryStart = previousMonth.atDay(1).atStartOfDay();
    LocalDateTime salaryEnd = previousMonth.plusMonths(1).atDay(1).atStartOfDay();

    LocalDateTime manualStart = yearMonth.atDay(1).atStartOfDay();
    LocalDateTime manualEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

    Account salaryAccount = findSalaryAccount();
    AccountId accountId = salaryAccount.getAccountId();

    BigDecimal salaryIncome =
        safe(accountTransactionRepository.sumMonthlyIncome(accountId, salaryStart, salaryEnd));

    Long serviceUserId = requesterInfo.getServiceUserId();
    BigDecimal manualIncome =
        safe(
            manualLedgerRepository.sumAmountByUserIdAndTypeBetween(
                serviceUserId,
                ManualLedgerType.INCOME,
                manualStart.toLocalDate(),
                manualEnd.toLocalDate()));

    BigDecimal monthlyRepayment = sumMonthlyRepayment();

    // 사용가능한 금액
    BigDecimal availableIncome =
        salaryIncome.add(manualIncome).subtract(monthlyRepayment).max(ZERO);

    // 사용가능한 자유 지출
    BigDecimal variableSpendingBudget =
        availableIncome.multiply(BUDGET_RATIO).setScale(0, RoundingMode.DOWN);

    // 총 카테고리별 추천 금액 계산 로직
    Map<ConsumptionCategory, BigDecimal> categoryRecommendation =
        buildCategoryRecommendation(variableSpendingBudget);

    return new RecommendedSpending(variableSpendingBudget, categoryRecommendation);
  }

  private void validateMonth(int month) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("월(month)은 1에서 12 사이의 값이어야 합니다.");
    }
  }

  // 급여 통장 찾기
  private Account findSalaryAccount() {
    Long coreBankingUserId = requesterInfo.getCoreBankingUserId();
    User user =
        userRepository
            .findById(UserId.of(coreBankingUserId))
            .orElseThrow(() -> new SalaryAccountNotFoundException(coreBankingUserId));

    return accountRepository.findAllByUser(user).stream()
        .filter(Account::isForIncome)
        .findFirst()
        .orElseThrow(() -> new SalaryAccountNotFoundException(coreBankingUserId));
  }

  // 월 상환금 더하기 (모든 대출)
  private BigDecimal sumMonthlyRepayment() {
    List<LoanDetail> loanDetails = loanReader.findLoanDetails();

    return loanDetails.stream()
        .map(LoanDetail::getMonthlyRepayment)
        .filter(Objects::nonNull)
        .reduce(ZERO, BigDecimal::add);
  }

  private BigDecimal safe(BigDecimal value) {
    return value == null ? ZERO : value;
  }

  // 카테고리별 비율을 정해 금액 도출
  private Map<ConsumptionCategory, BigDecimal> buildCategoryRecommendation(
      BigDecimal variableBudget) {

    Map<ConsumptionCategory, BigDecimal> result = new EnumMap<>(ConsumptionCategory.class);

    BigDecimal food = allocate(variableBudget, FOOD_RATIO);
    BigDecimal transport = allocate(variableBudget, TRANSPORT_RATIO);
    BigDecimal shopping = allocate(variableBudget, SHOPPING_RATIO);
    BigDecimal entertainment = allocate(variableBudget, ENTERTAINMENT_RATIO);

    BigDecimal remainder =
        variableBudget.subtract(food.add(transport).add(shopping).add(entertainment));
    // 소수점 계산하고 남은 금액 여가에 몰아주기
    if (remainder.compareTo(ZERO) != 0) {
      entertainment = entertainment.add(remainder);
    }

    result.put(ConsumptionCategory.FOOD, food);
    result.put(ConsumptionCategory.TRANSPORT, transport);
    result.put(ConsumptionCategory.SHOPPING, shopping);
    result.put(ConsumptionCategory.ENTERTAINMENT, entertainment);

    return result;
  }

  private BigDecimal allocate(BigDecimal total, BigDecimal ratio) {
    if (total.compareTo(ZERO) == 0) {
      return ZERO;
    }
    return total.multiply(ratio).setScale(0, RoundingMode.DOWN);
  }
}
