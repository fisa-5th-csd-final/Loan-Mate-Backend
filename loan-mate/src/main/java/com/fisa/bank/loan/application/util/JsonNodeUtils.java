package com.fisa.bank.loan.application.util;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodeUtils {
  public static BigDecimal getBigDecimal(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (value.isMissingNode()) return null;
    return new BigDecimal(value.asText("0"));
  }

  public static <T extends Enum<T>> T getEnum(JsonNode node, String field, Class<T> enumType) {
    JsonNode value = node.path(field);
    if (value.isMissingNode()) return null;
    try {
      return Enum.valueOf(enumType, value.asText());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
