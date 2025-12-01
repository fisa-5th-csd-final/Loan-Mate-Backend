package com.fisa.bank.accountbook.persistence;

import org.springframework.stereotype.Component;

import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.persistence.entity.ManualLedgerEntity;

@Component
public class ManualLedgerMapper {

  public ManualLedgerEntry toDomain(ManualLedgerEntity entity) {
    if (entity == null) {
      return null;
    }
    return new ManualLedgerEntry(
        entity.getId(),
        entity.getServiceUserId(),
        entity.getType(),
        entity.getAmount(),
        entity.getDescription(),
        entity.getSavedAt(),
        entity.getCategory());
  }

  public ManualLedgerEntity toEntity(ManualLedgerEntry entry) {
    if (entry == null) {
      return null;
    }
    return ManualLedgerEntity.builder()
        .id(entry.id())
        .serviceUserId(entry.serviceUserId())
        .type(entry.type())
        .amount(entry.amount())
        .description(entry.description())
        .savedAt(entry.savedAt())
        .category(entry.category())
        .build();
  }
}
