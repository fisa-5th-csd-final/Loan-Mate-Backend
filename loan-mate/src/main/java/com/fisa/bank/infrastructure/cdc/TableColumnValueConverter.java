package com.fisa.bank.infrastructure.cdc;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * CDC payload 에 들어있는 값을 대상 테이블의 컬럼 타입에 맞게 변환한다.
 */
@Component
class TableColumnValueConverter {

  private static final long MICROS_PER_SECOND = 1_000_000L;
  private static final long NANOS_PER_MICRO = 1_000L;
  private static final long MICROS_PER_DAY = 86_400_000_000L;

  Object convert(TableColumnMetadataProvider.ColumnMetadata metadata, Object value) {
    if (metadata == null || value == null) {
      return value;
    }
    String dataType = metadata.dataType();
    if (!StringUtils.hasText(dataType)) {
      return value;
    }
    return switch (dataType) {
      case "datetime", "timestamp" -> toTimestamp(value);
      case "date" -> toDate(value);
      case "time" -> toTime(value);
      case "decimal", "numeric" -> toBigDecimal(value);
      case "json" -> toJsonString(value);
      default -> value;
    };
  }

  private Object toTimestamp(Object value) {
    if (value instanceof Timestamp) {
      return value;
    }
    if (value instanceof Number number) {
      return toTimestampFromMicros(number.longValue());
    }
    if (value instanceof String str) {
      if (!StringUtils.hasText(str)) {
        return null;
      }
      if (isNumeric(str)) {
        try {
          long micros = Long.parseLong(str);
          return toTimestampFromMicros(micros);
        } catch (NumberFormatException ignored) {
        }
      }
      try {
        return Timestamp.valueOf(str.replace('T', ' ').replace("Z", ""));
      } catch (IllegalArgumentException ex) {
        try {
          Instant instant = Instant.parse(str);
          return Timestamp.from(instant);
        } catch (DateTimeParseException ignored) {
          return value;
        }
      }
    }
    return value;
  }

  private Timestamp toTimestampFromMicros(long micros) {
    long seconds = micros / MICROS_PER_SECOND;
    long microsPart = micros % MICROS_PER_SECOND;
    Instant instant = Instant.ofEpochSecond(seconds, microsPart * NANOS_PER_MICRO);
    return Timestamp.from(instant);
  }

  private Object toDate(Object value) {
    if (value instanceof Date) {
      return value;
    }
    if (value instanceof Number number) {
      long days = number.longValue();
      return Date.valueOf(LocalDate.ofEpochDay(days));
    }
    if (value instanceof String str) {
      if (!StringUtils.hasText(str)) {
        return null;
      }
      if (isNumeric(str)) {
        try {
          return Date.valueOf(LocalDate.ofEpochDay(Long.parseLong(str)));
        } catch (NumberFormatException ignored) {
        }
      }
      try {
        return Date.valueOf(str);
      } catch (IllegalArgumentException ex) {
        return value;
      }
    }
    return value;
  }

  private Object toTime(Object value) {
    if (value instanceof Time) {
      return value;
    }
    if (value instanceof Number number) {
      long micros = number.longValue();
      if (Math.abs(micros) >= MICROS_PER_DAY) {
        return value;
      }
      long nanos = micros * NANOS_PER_MICRO;
      LocalTime time = LocalTime.ofNanoOfDay(nanos);
      return Time.valueOf(time);
    }
    if (value instanceof String str) {
      if (!StringUtils.hasText(str)) {
        return null;
      }
      if (isNumeric(str)) {
        try {
          long micros = Long.parseLong(str);
          if (Math.abs(micros) >= MICROS_PER_DAY) {
            return value;
          }
          LocalTime time = LocalTime.ofNanoOfDay(micros * NANOS_PER_MICRO);
          return Time.valueOf(time);
        } catch (NumberFormatException ignored) {
        }
      }
      try {
        return Time.valueOf(str);
      } catch (IllegalArgumentException ex) {
        return value;
      }
    }
    return value;
  }

  private Object toBigDecimal(Object value) {
    if (value instanceof BigDecimal) {
      return value;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString());
    }
    if (value instanceof String str && StringUtils.hasText(str)) {
      try {
        return new BigDecimal(str);
      } catch (NumberFormatException ignored) {
        return value;
      }
    }
    return value;
  }

  private Object toJsonString(Object value) {
    if (value instanceof String str) {
      return str;
    }
    return value != null ? value.toString() : null;
  }

  private boolean isNumeric(String value) {
    if (!StringUtils.hasText(value)) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (!Character.isDigit(ch) && !(i == 0 && (ch == '-' || ch == '+'))) {
        return false;
      }
    }
    return true;
  }
}
