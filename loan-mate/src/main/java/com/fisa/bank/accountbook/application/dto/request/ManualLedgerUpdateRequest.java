package com.fisa.bank.accountbook.application.dto.request;

import java.math.BigDecimal;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public record ManualLedgerUpdateRequest(
    ManualLedgerType type, BigDecimal amount, String description) {}
