package com.fisa.bank.accountbook.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ManualLedgerEntry(
    Long id,
    Long serviceUserId,
    ManualLedgerType type,
    BigDecimal amount,
    String description,
    LocalDate savedAt) {}
