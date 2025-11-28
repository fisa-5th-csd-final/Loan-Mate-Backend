package com.fisa.bank.loan.presentation.controller;

import com.fisa.bank.loan.application.dto.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ResponseCode;
import com.fisa.bank.loan.application.dto.request.AutoDepositUpdateRequest;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
@Slf4j
public class LoanController {
  private final ManageLoanUseCase manageLoanUseCase;

  @GetMapping("/ledgers")
  public ApiResponse<SuccessBody<List<LoanListResponse>>> getLoans() {
    List<LoanListResponse> response = manageLoanUseCase.getLoans();
    return ApiResponseGenerator.success(ResponseCode.GET, response);
  }

  @GetMapping("/ledger/{loanId:\\d+}")
  public ApiResponse<SuccessBody<LoanDetailResponse>> getLoanDetail(@PathVariable Long loanId) {
    log.info("대출 세부 정보 조회");
    LoanDetailResponse loanDetail = manageLoanUseCase.getLoanDetail(loanId);
    return ApiResponseGenerator.success(ResponseCode.GET, loanDetail);
  }

  @GetMapping("/ledgers/{loanId}/auto-deposit")
  public ApiResponse<SuccessBody<LoanAutoDepositResponse>> getAutoDeposit(
      @PathVariable Long loanId) {
    log.info("자동 예치 여부 조회");
    LoanAutoDepositResponse response = manageLoanUseCase.getAutoDeposit(loanId);

    return ApiResponseGenerator.success(ResponseCode.GET, response);
  }

  @DeleteMapping("{loanId:\\d+}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLoan(@PathVariable("loanId") Long loanId) {
    log.info("대출 해지");
    manageLoanUseCase.cancelLoan(loanId);
  }

  @GetMapping("/prepayment-infos")
  public ApiResponse<SuccessBody<List<LoansWithPrepaymentBenefitResponse>>>
      getLoansWithPrepaymentBenefit() {
    log.info("선납 이득인 대출 조회");
    List<LoansWithPrepaymentBenefitResponse> loansWithPrepaymentBenefit =
        manageLoanUseCase.getLoansWithPrepaymentBenefit();
    return ApiResponseGenerator.success(ResponseCode.GET, loansWithPrepaymentBenefit);
  }

  @PatchMapping("/ledgers/{loanId}/auto-deposit")
  public ApiResponse<SuccessBody<Void>> updateAutoDeposit(
      @PathVariable Long loanId, @RequestBody AutoDepositUpdateRequest request) {
    log.info("자동 예치 여부 수정");
    manageLoanUseCase.updateAutoDepositEnabled(loanId, request.isAutoDepositEnabled());

    return ApiResponseGenerator.success(ResponseCode.UPDATE);
  }

  @GetMapping("/auto-deposit-summary")
  public ApiResponse<SuccessBody<List<AutoDepositResponse>>> getUserLoanSimpleSummary() {
    log.info("자동 예치 정보 조회");

    List<AutoDepositResponse> summary = manageLoanUseCase.getAutoDepositSummary();

    return ApiResponseGenerator.success(ResponseCode.GET, summary);
  }
}
