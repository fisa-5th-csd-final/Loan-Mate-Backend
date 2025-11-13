package com.fisa.bank.loan.presentation.controller;

import com.fisa.bank.common.presentation.response.ApiResponse;
import com.fisa.bank.common.presentation.response.ApiResponseGenerator;
import com.fisa.bank.common.presentation.response.body.SuccessBody;
import com.fisa.bank.common.presentation.response.code.ApiResponseCode;
import com.fisa.bank.common.presentation.response.code.ResponseCode;
import com.fisa.bank.loan.application.domain.Loan;
import com.fisa.bank.loan.application.usecase.ManageLoanUseCase;
import com.fisa.bank.loan.presentation.controller.dto.response.LoanListResponse;
import jakarta.persistence.PreRemove;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/loans")
public class LoanController {
    private final ManageLoanUseCase manageLoanUseCase;

    @GetMapping("/ledgers")
    public ApiResponse<SuccessBody<List<LoanListResponse>>> getLoans(){
        // TODO: 로그인 완성되면 파라미터 제거
        List<LoanListResponse> response = manageLoanUseCase.getLoans(Long.valueOf(1))
                .stream()
                .map(LoanListResponse::from)
                .toList();
        return ApiResponseGenerator.success(ResponseCode.GET,response);
    }
}
