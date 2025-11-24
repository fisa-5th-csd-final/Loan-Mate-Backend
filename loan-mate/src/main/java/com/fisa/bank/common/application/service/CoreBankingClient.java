package com.fisa.bank.common.application.service;

import java.util.List;

public interface CoreBankingClient {
  <T> T fetchOne(String endpoint, Class<T> clazz);

  <T> List<T> fetchList(String endpoint, Class<T> clazz);

  public void fetchOneDelete(String endpoint);

  void patch(String endpoint, Object body);
}
