package com.fisa.bank.accountbook.application.service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerCreateRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.exception.ManualLedgerInvalidRequestException;
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
    validate(request);

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

  private void validate(ManualLedgerCreateRequest request) {
    if (request == null) {
      throw new ManualLedgerInvalidRequestException("요청 데이터가 비어 있습니다.");
    }
    if (request.type() == null) {
      throw new ManualLedgerInvalidRequestException("수입/지출 유형을 선택해야 합니다.");
    }
    if (request.amount() == null
        || request.amount().compareTo(BigDecimal.ZERO) <= 0
        || request.amount().scale() > 2) {
      throw new ManualLedgerInvalidRequestException("금액은 0보다 큰 값이어야 하며 소수점 둘째 자리까지 입력 가능합니다.");
    }
    if (request.savedAt() == null) {
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
}
