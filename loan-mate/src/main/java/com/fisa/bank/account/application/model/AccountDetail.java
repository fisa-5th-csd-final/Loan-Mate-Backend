package com.fisa.bank.account.application.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountDetail(
    Long accountId,
    String accountNumber,
    String bankCode,
    BigDecimal balance,
    LocalDateTime createdAt,
    boolean isForIncome) {}
