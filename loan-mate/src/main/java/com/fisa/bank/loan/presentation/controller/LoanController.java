package com.fisa.bank.loan.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ResponseCode;
import com.fisa.bank.loan.application.dto.response.LoanDetailResponse;
import com.fisa.bank.loan.application.dto.response.LoanListResponse;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
@Slf4j
public class LoanController {
  private final ManageLoanUseCase manageLoanUseCase;

  @GetMapping("/ledgers")
  public ApiResponse<SuccessBody<List<LoanListResponse>>> getLoans() {
    // TODO: 로그인 완성되면 파라미터 제거
    List<LoanListResponse> response = manageLoanUseCase.getLoans(Long.valueOf(1));
    return ApiResponseGenerator.success(ResponseCode.GET, response);
  }

  @GetMapping("/ledger/{loanId:\\d+}")
  public ApiResponse<SuccessBody<LoanDetailResponse>> getLoanDetail(@PathVariable Long loanId) {
    log.info("대출 세부 정보 조회");
    LoanDetailResponse loanDetail = manageLoanUseCase.getLoanDetail(loanId);
    return ApiResponseGenerator.success(ResponseCode.GET, loanDetail);
  }

  @DeleteMapping("{loanId:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLoan(@PathVariable("loanId") Long loanId) {
    log.info("대출 해지");
    manageLoanUseCase.cancelLoan(loanId);
  }
}
