package com.fisa.bank.accountbook.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

public record ManualLedgerResponse(
    Long id,
    ManualLedgerType type,
    BigDecimal amount,
    String description,
    LocalDate savedAt,
    ConsumptionCategory category) {

  public static ManualLedgerResponse from(ManualLedgerEntry entry) {
    return new ManualLedgerResponse(
        entry.id(),
        entry.type(),
        entry.amount(),
        entry.description(),
        entry.savedAt(),
        entry.category());
  }
}
