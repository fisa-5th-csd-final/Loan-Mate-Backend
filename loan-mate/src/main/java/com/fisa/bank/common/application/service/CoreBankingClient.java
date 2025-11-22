package com.fisa.bank.common.application.service;

import java.util.List;

public interface CoreBankingClient {
  <T> T fetchOne(String endpoint, Class<T> clazz);

  <T> List<T> fetchList(String endpoint, Class<T> clazz);

  void updateAutoDepositEnabled(Long loanLedgerId, boolean autoDepositEnabled);

  public void fetchOneDelete(String endpoint);
}
