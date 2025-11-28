package com.fisa.bank.accountbook.application.usecase;

import java.util.List;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerCreateRequest;
import com.fisa.bank.accountbook.application.dto.request.ManualLedgerUpdateRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public interface ManageManualLedgerUseCase {

  ManualLedgerResponse addEntry(ManualLedgerCreateRequest request);

  List<ManualLedgerResponse> getEntries(ManualLedgerType type);

  ManualLedgerResponse updateEntry(Long entryId, ManualLedgerUpdateRequest request);

  void deleteEntry(Long entryId);
}
