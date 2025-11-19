package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fisa.bank.account.application.model.MonthlySpending;
import com.fisa.bank.account.application.usecase.GetMonthlySpendingUseCase;

@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
public class SpendingController {

  private final GetMonthlySpendingUseCase getMonthlySpendingUseCase;

  @GetMapping("/{accountId}/{year}/{month}")
  public MonthlySpending getMonthlySpending(
      @PathVariable Long accountId, @PathVariable int year, @PathVariable int month) {

    return getMonthlySpendingUseCase.execute(accountId, year, month);
  }
}
