package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fisa.bank.account.application.model.MonthlySpending;
import com.fisa.bank.account.application.model.RecommendedSpending;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;
import com.fisa.bank.account.application.usecase.GetRecommendedSpendingUseCase;

@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
public class SpendingController {

  private final GetMonthlySpendingUseCase getMonthlySpendingUseCase;
  private final GetRecommendedSpendingUseCase getRecommendedSpendingUseCase;

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
}
