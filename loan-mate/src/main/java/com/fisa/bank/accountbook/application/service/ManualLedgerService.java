package com.fisa.bank.accountbook.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerCreateRequest;
import com.fisa.bank.accountbook.application.dto.request.ManualLedgerUpdateRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.exception.ManualLedgerAccessDeniedException;
import com.fisa.bank.accountbook.application.exception.ManualLedgerInvalidRequestException;
import com.fisa.bank.accountbook.application.exception.ManualLedgerNotFoundException;
import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.accountbook.application.usecase.ManageManualLedgerUseCase;
import com.fisa.bank.common.application.util.RequesterInfo;

@Service
@RequiredArgsConstructor
public class ManualLedgerService implements ManageManualLedgerUseCase {

  private final ManualLedgerRepository manualLedgerRepository;
  private final RequesterInfo requesterInfo;

  @Override
  public ManualLedgerResponse addEntry(ManualLedgerCreateRequest request) {
    validateCreate(request);

    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry entry =
        new ManualLedgerEntry(
            null,
            userId,
            request.type(),
            request.amount(),
            trimDescription(request.description()),
            request.savedAt());

    ManualLedgerEntry saved = manualLedgerRepository.save(entry);
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
    validateUpdate(request);
    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry ownedEntry = getOwnedEntry(entryId, userId);

    ManualLedgerEntry updatedEntry =
        new ManualLedgerEntry(
            ownedEntry.id(),
            ownedEntry.serviceUserId(),
            request.type(),
            request.amount(),
            trimDescription(request.description()),
            ownedEntry.savedAt());

    ManualLedgerEntry saved = manualLedgerRepository.save(updatedEntry);
    return ManualLedgerResponse.from(saved);
  }

  @Override
  public void deleteEntry(Long entryId) {
    Long userId = requesterInfo.getServiceUserId();
    ManualLedgerEntry ownedEntry = getOwnedEntry(entryId, userId);
    manualLedgerRepository.deleteById(ownedEntry.id());
  }

  private void validateCreate(ManualLedgerCreateRequest request) {
    if (request == null) {
      throw new ManualLedgerInvalidRequestException("요청 데이터가 비어 있습니다.");
    }
    validateCommon(request.type(), request.amount());
    validateSavedAt(request.savedAt());
  }

  private void validateUpdate(ManualLedgerUpdateRequest request) {
    if (request == null) {
      throw new ManualLedgerInvalidRequestException("요청 데이터가 비어 있습니다.");
    }
    validateCommon(request.type(), request.amount());
  }

  private void validateCommon(ManualLedgerType type, BigDecimal amount) {
    if (type == null) {
      throw new ManualLedgerInvalidRequestException("수입/지출 유형을 선택해야 합니다.");
    }
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.scale() > 2) {
      throw new ManualLedgerInvalidRequestException("금액은 0보다 큰 값이어야 하며 소수점 둘째 자리까지 입력 가능합니다.");
    }
  }

  private void validateSavedAt(LocalDate savedAt) {
    if (savedAt == null) {
      throw new ManualLedgerInvalidRequestException("저장 일자를 입력해야 합니다.");
    }
  }

  private String trimDescription(String description) {
    if (description == null) {
      return null;
    }
    String trimmed = description.trim();
    return trimmed.isEmpty() ? null : trimmed;
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
}
