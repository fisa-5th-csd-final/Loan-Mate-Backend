package com.fisa.bank.account.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferRequest(

        @NotBlank(message = "보내는 계좌번호는 필수입니다.")
        String fromAccountNumber,

        @NotBlank(message = "받는 계좌번호는 필수입니다.")
        String toAccountNumber,

        @NotBlank(message = "받는 은행 코드는 필수입니다.")
        String toBankCode,

        @NotNull(message = "송금 금액은 null일 수 없습니다.")
        @Positive(message = "송금 금액은 0보다 커야 합니다.")
        BigDecimal amount

) {}
