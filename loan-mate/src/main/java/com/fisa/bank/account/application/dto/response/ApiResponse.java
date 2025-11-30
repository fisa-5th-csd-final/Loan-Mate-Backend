package com.fisa.bank.account.application.dto.response;

public record ApiResponse<T>(
        String code,
        String message,
        T data
) {}
