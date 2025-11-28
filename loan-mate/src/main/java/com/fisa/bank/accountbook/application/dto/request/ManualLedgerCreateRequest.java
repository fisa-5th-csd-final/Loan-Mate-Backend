package com.fisa.bank.accountbook.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public record ManualLedgerCreateRequest(
    ManualLedgerType type, BigDecimal amount, String description, LocalDate savedAt) {}
