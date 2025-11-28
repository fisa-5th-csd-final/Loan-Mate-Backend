package com.fisa.bank.accountbook.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.usecase.ManageManualLedgerUseCase;
import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ResponseCode;

@RestController
@RequestMapping("/api/manual-ledgers")
@RequiredArgsConstructor
@Slf4j
public class ManualLedgerController {

  private final ManageManualLedgerUseCase manageManualLedgerUseCase;

  @PostMapping
  public ApiResponse<SuccessBody<ManualLedgerResponse>> addEntry(
      @RequestBody ManualLedgerRequest request) {
    log.info("사용자가 수입/지출 등록 요청");
    ManualLedgerResponse response = manageManualLedgerUseCase.addEntry(request);
    return ApiResponseGenerator.success(ResponseCode.CREATE, response);
  }

  @GetMapping
  public ApiResponse<SuccessBody<List<ManualLedgerResponse>>> getEntries(
      @RequestParam(value = "type", required = false) ManualLedgerType type) {
    log.info("사용자가 수입/지출 조회 요청, type={}", type);
    List<ManualLedgerResponse> entries = manageManualLedgerUseCase.getEntries(type);
    return ApiResponseGenerator.success(ResponseCode.GET, entries);
  }

  @PutMapping("/{entryId}")
  public ApiResponse<SuccessBody<ManualLedgerResponse>> updateEntry(
      @PathVariable Long entryId, @RequestBody ManualLedgerRequest request) {
    log.info("사용자가 수입/지출 수정 요청, id={}", entryId);
    ManualLedgerResponse response = manageManualLedgerUseCase.updateEntry(entryId, request);
    return ApiResponseGenerator.success(ResponseCode.UPDATE, response);
  }

  @DeleteMapping("/{entryId}")
  public ApiResponse<SuccessBody<Void>> deleteEntry(@PathVariable Long entryId) {
    log.info("사용자가 수입/지출 삭제 요청, id={}", entryId);
    manageManualLedgerUseCase.deleteEntry(entryId);
    return ApiResponseGenerator.success(ResponseCode.DELETE);
  }
}
