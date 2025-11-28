package com.fisa.bank.accountbook.application.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public record ManualLedgerCreateRequest(
    @NotNull ManualLedgerType type,
    @NotNull @Positive @Digits(integer = 18, fraction = 2) BigDecimal amount,
    String description,
    @NotNull LocalDate savedAt) {}
