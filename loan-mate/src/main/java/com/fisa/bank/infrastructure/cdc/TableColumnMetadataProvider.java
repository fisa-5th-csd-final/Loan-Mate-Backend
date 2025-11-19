package com.fisa.bank.infrastructure.cdc;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** 대상 테이블의 컬럼 타입 메타데이터를 캐싱해서 제공한다. */
@Component
@RequiredArgsConstructor
class TableColumnMetadataProvider {

  private static final String COLUMN_METADATA_SQL =
      """
      SELECT COLUMN_NAME, DATA_TYPE, DATETIME_PRECISION, NUMERIC_SCALE
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = ?
        AND TABLE_NAME = ?
      """;

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, Map<String, ColumnMetadata>> cache = new ConcurrentHashMap<>();
  private volatile String defaultSchema;

  Map<String, ColumnMetadata> getColumnMetadata(String schema, String table) {
    if (!StringUtils.hasText(table)) {
      return Map.of();
    }
    String resolvedSchema = resolveSchema(schema);
    if (!StringUtils.hasText(resolvedSchema)) {
      return Map.of();
    }
    String cacheKey = resolvedSchema + "." + table;
    return cache.computeIfAbsent(cacheKey, key -> loadColumns(resolvedSchema, table));
  }

  private Map<String, ColumnMetadata> loadColumns(String schema, String table) {
    Map<String, ColumnMetadata> result = new LinkedHashMap<>();
    jdbcTemplate.query(
        COLUMN_METADATA_SQL,
        rs -> {
          String columnName = rs.getString("COLUMN_NAME");
          String dataType = rs.getString("DATA_TYPE");
          BigDecimal datetimePrecisionValue = rs.getBigDecimal("DATETIME_PRECISION");
          Integer datetimePrecision =
              datetimePrecisionValue != null ? datetimePrecisionValue.intValue() : null;
          BigDecimal numericScaleValue = rs.getBigDecimal("NUMERIC_SCALE");
          Integer numericScale = numericScaleValue != null ? numericScaleValue.intValue() : null;
          result.put(
              columnName,
              new ColumnMetadata(columnName, normalize(dataType), datetimePrecision, numericScale));
        },
        schema,
        table);
    return Map.copyOf(result);
  }

  private String normalize(String dataType) {
    if (!StringUtils.hasText(dataType)) {
      return "";
    }
    return dataType.toLowerCase(Locale.ROOT);
  }

  private String resolveSchema(String schema) {
    if (StringUtils.hasText(schema)) {
      return schema;
    }
    String candidate = defaultSchema;
    if (!StringUtils.hasText(candidate)) {
      synchronized (this) {
        if (!StringUtils.hasText(defaultSchema)) {
          defaultSchema = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        }
        candidate = defaultSchema;
      }
    }
    return candidate;
  }

  record ColumnMetadata(
      String columnName, String dataType, Integer datetimePrecision, Integer numericScale) {}
}
