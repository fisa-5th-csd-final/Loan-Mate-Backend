package com.fisa.bank.accountbook.application.service;

import lombok.RequiredArgsConstructor;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerCreateRequest;
import com.fisa.bank.accountbook.application.dto.request.ManualLedgerUpdateRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.exception.ManualLedgerAccessDeniedException;
import com.fisa.bank.accountbook.application.exception.ManualLedgerNotFoundException;
import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.accountbook.application.usecase.ManageManualLedgerUseCase;
import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.persistence.account.enums.ConsumptionCategory;

@Service
@RequiredArgsConstructor
public class ManualLedgerService implements ManageManualLedgerUseCase {

  private final ManualLedgerRepository manualLedgerRepository;
  private final RequesterInfo requesterInfo;
  private final CacheManager cacheManager;

  @Override
  public ManualLedgerResponse addEntry(ManualLedgerCreateRequest request) {

    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry entry =
        new ManualLedgerEntry(
            null,
            userId,
            request.type(),
            request.amount(),
            trimDescription(request.description()),
            request.savedAt(),
            resolveCategory(request.type(), request.category()));

    ManualLedgerEntry saved = manualLedgerRepository.save(entry);
    evictAiExpenditureCache(userId, entry.savedAt());
    return ManualLedgerResponse.from(saved);
  }

  @Override
  public List<ManualLedgerResponse> getEntries(ManualLedgerType type) {
    Long userId = requesterInfo.getServiceUserId();
    List<ManualLedgerEntry> entries =
        type == null
            ? manualLedgerRepository.findByUserId(userId)
            : manualLedgerRepository.findByUserIdAndType(userId, type);

    return entries.stream().map(ManualLedgerResponse::from).toList();
  }

  @Override
  public ManualLedgerResponse updateEntry(Long entryId, ManualLedgerUpdateRequest request) {
    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry ownedEntry = getOwnedEntry(entryId, userId);

    ManualLedgerEntry updatedEntry =
        new ManualLedgerEntry(
            ownedEntry.id(),
            ownedEntry.serviceUserId(),
            ownedEntry.type(),
            request.amount(),
            trimDescription(request.description()),
            ownedEntry.savedAt(),
            resolveCategory(ownedEntry.type(), request.category()));

    ManualLedgerEntry saved = manualLedgerRepository.save(updatedEntry);
    evictAiExpenditureCache(userId, ownedEntry.savedAt());
    return ManualLedgerResponse.from(saved);
  }

  @Override
  public void deleteEntry(Long entryId) {
    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry ownedEntry = getOwnedEntry(entryId, userId);
    evictAiExpenditureCache(userId, ownedEntry.savedAt());
    manualLedgerRepository.deleteById(ownedEntry.id());
  }

  private String trimDescription(String description) {
    if (description == null) {
      return null;
    }
    String trimmed = description.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private ConsumptionCategory resolveCategory(
      ManualLedgerType type, ConsumptionCategory consumptionCategory) {
    if (type == ManualLedgerType.INCOME) {
      return null;
    }
    return consumptionCategory;
  }

  private ManualLedgerEntry getOwnedEntry(Long entryId, Long userId) {
    ManualLedgerEntry entry =
        manualLedgerRepository
            .findById(entryId)
            .orElseThrow(() -> new ManualLedgerNotFoundException(entryId));

    if (!Objects.equals(entry.serviceUserId(), userId)) {
      throw new ManualLedgerAccessDeniedException(entryId);
    }

    return entry;
  }

  private void evictAiExpenditureCache(Long serviceUserId, java.time.LocalDate savedAt) {
    var cache = cacheManager.getCache("aiExpenditure");
    if (cache == null) {
      return;
    }
    cache.evict(serviceUserId + ":" + YearMonth.from(savedAt));
  }
}
