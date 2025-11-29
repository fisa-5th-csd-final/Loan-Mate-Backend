package com.fisa.bank.accountbook.application.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public interface ManualLedgerRepository {

  ManualLedgerEntry save(ManualLedgerEntry entry);

  List<ManualLedgerEntry> findByUserId(Long serviceUserId);

  List<ManualLedgerEntry> findByUserIdAndType(Long serviceUserId, ManualLedgerType type);

  Optional<ManualLedgerEntry> findById(Long id);

  void deleteById(Long id);

  BigDecimal sumAmountByUserIdAndTypeBetween(
      Long serviceUserId, ManualLedgerType type, LocalDate startDate, LocalDate endDate);
}
