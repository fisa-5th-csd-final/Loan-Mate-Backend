package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.*;

import com.fisa.bank.account.application.dto.request.SpendingLimitRequest;
import com.fisa.bank.account.application.dto.response.SpendingLimitResponse;
import com.fisa.bank.account.application.model.spending.MonthlySpending;
import com.fisa.bank.account.application.model.spending.RecommendedSpending;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;
import com.fisa.bank.account.application.usecase.GetRecommendedSpendingUseCase;
import com.fisa.bank.account.application.usecase.SaveUserSpendingLimitUseCase;

@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
public class SpendingController {

  private final GetMonthlySpendingUseCase getMonthlySpendingUseCase;
  private final GetRecommendedSpendingUseCase getRecommendedSpendingUseCase;
  private final SaveUserSpendingLimitUseCase saveUserSpendingLimitUseCase;

  @GetMapping("/{accountId}/{year}/{month}")
  public MonthlySpending getMonthlySpending(
      @PathVariable Long accountId, @PathVariable int year, @PathVariable int month) {

    return getMonthlySpendingUseCase.execute(accountId, year, month);
  }

  @GetMapping("/recommended")
  public RecommendedSpending getRecommendedSpending(
      @RequestParam int month, @RequestParam(required = false) Integer year) {

    int targetYear = year != null ? year : LocalDate.now().getYear();
    return getRecommendedSpendingUseCase.execute(targetYear, month);
  }

  @PutMapping("/limits")
  public SpendingLimitResponse updateSpendingLimit(
      @RequestBody(required = false) SpendingLimitRequest request) {

    var saved =
        saveUserSpendingLimitUseCase.execute(request != null ? request.userLimitRatio() : null);
    return new SpendingLimitResponse(saved.limits());
  }
}
