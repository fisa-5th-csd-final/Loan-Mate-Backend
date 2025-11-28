package com.fisa.bank.accountbook.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public record ManualLedgerResponse(
    Long id, ManualLedgerType type, BigDecimal amount, String description, LocalDate savedAt) {

  public static ManualLedgerResponse from(ManualLedgerEntry entry) {
    return new ManualLedgerResponse(
        entry.id(), entry.type(), entry.amount(), entry.description(), entry.savedAt());
  }
}
