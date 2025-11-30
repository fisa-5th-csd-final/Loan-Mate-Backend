package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.account.application.service.AiExpenditureService;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

  private final AiExpenditureService aiExpenditureService;

  @PostMapping("/expenditure")
  public JsonNode expenditure(
      @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {

    return aiExpenditureService.requestExpenditure(year, month);
  }
}
