package com.fisa.bank.common.application.util.core_bank;

import java.util.List;

public interface CoreBankingClient {
  <T> T fetchOne(String endpoint, Class<T> clazz);

  <T> List<T> fetchList(String endpoint, Class<T> clazz);

  public void delete(String endpoint);

  void patch(String endpoint, Object body);
}
