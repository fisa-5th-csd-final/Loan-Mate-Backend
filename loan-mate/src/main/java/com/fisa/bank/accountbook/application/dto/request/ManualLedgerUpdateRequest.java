package com.fisa.bank.accountbook.application.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record ManualLedgerUpdateRequest(
    @NotNull @Positive @Digits(integer = 18, fraction = 2) BigDecimal amount,
    String description,
    ConsumptionCategory category) {}
