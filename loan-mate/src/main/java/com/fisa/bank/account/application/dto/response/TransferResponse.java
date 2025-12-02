package com.fisa.bank.account.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
    String fromAccountNumber,
    String toAccountNumber,
    BigDecimal amount,
    BigDecimal fromBalanceAfter,
    BigDecimal toBalanceAfter,
    LocalDateTime transactionAt,
    String message) {}
