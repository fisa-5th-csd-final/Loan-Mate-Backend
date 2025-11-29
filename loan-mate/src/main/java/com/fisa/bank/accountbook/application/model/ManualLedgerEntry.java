package com.fisa.bank.accountbook.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record ManualLedgerEntry(
    Long id,
    Long serviceUserId,
    ManualLedgerType type,
    BigDecimal amount,
    String description,
    LocalDate savedAt,
    ConsumptionCategory category) {}
