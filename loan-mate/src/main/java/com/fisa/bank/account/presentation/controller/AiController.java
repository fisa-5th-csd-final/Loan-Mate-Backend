package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.dto.request.SpendingLimitRequest;
import com.fisa.bank.account.application.dto.response.SpendingLimitResponse;
import com.fisa.bank.account.application.service.AiExpenditureService;
import com.fisa.bank.account.application.usecase.SaveUserSpendingLimitUseCase;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

  private final AiExpenditureService aiExpenditureService;
  private final SaveUserSpendingLimitUseCase saveUserSpendingLimitUseCase;

  @PutMapping("/expenditure/limits")
  public SpendingLimitResponse updateSpendingLimit(
      @RequestBody(required = false) SpendingLimitRequest request) {

    var saved =
        saveUserSpendingLimitUseCase.execute(request != null ? request.userLimitRatio() : null);
    return new SpendingLimitResponse(saved.limits());
  }

  @PostMapping("/expenditure")
  public JsonNode expenditure(
      @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {

    return aiExpenditureService.requestExpenditure(year, month);
  }
}
