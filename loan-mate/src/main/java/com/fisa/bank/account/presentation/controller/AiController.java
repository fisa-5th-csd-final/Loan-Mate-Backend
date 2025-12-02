package com.fisa.bank.account.presentation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fisa.bank.account.application.dto.response.AiExpenditureResponse;
import com.fisa.bank.account.application.service.ai.AiExpenditureService;
import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ResponseCode;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

  private final AiExpenditureService aiExpenditureService;

  @GetMapping("/expenditure")
  public ApiResponse<SuccessBody<AiExpenditureResponse>> expenditure(
      @RequestParam(required = false) Integer year, @RequestParam(required = false) Integer month) {

    AiExpenditureResponse response = aiExpenditureService.requestExpenditure(year, month);
    return ApiResponseGenerator.success(ResponseCode.GET, response);
  }
}
