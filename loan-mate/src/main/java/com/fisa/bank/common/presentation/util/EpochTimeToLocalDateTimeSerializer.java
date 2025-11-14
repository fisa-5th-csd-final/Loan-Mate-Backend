package com.fisa.bank.common.presentation.util;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class EpochTimeToLocalDateTimeSerializer extends JsonDeserializer<LocalDateTime> {

  @Override
  public LocalDateTime deserialize(
      JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException, JacksonException {
    long ms = jsonParser.getLongValue();
    Instant instant = Instant.ofEpochMilli(ms);
    return LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
  }
}
