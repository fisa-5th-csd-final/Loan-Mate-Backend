package com.fisa.bank.common.application.util.ai;

public interface AiClient {
  <T, B> T fetchOne(String endpoint, B body, Class<T> clazz);
}
