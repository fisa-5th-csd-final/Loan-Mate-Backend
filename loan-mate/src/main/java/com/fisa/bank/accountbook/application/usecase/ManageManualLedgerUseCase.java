package com.fisa.bank.accountbook.application.usecase;

import java.util.List;

import com.fisa.bank.accountbook.application.dto.request.ManualLedgerRequest;
import com.fisa.bank.accountbook.application.dto.response.ManualLedgerResponse;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;

public interface ManageManualLedgerUseCase {

  ManualLedgerResponse addEntry(ManualLedgerRequest request);

  List<ManualLedgerResponse> getEntries(ManualLedgerType type);

  ManualLedgerResponse updateEntry(Long entryId, ManualLedgerRequest request);

  void deleteEntry(Long entryId);
}
