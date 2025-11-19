package com.fisa.bank.infrastructure.cdc;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 대상 테이블의 PK 정보를 Information Schema 에서 조회하고 캐싱한다.
 */
@Component
@RequiredArgsConstructor
class TablePrimaryKeyMetadataProvider {

  private static final String PRIMARY_KEY_SQL =
      """
      SELECT COLUMN_NAME
      FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA = ?
        AND TABLE_NAME = ?
        AND CONSTRAINT_NAME = 'PRIMARY'
      ORDER BY ORDINAL_POSITION
      """;

  private final JdbcTemplate jdbcTemplate;
  private final Map<String, List<String>> cache = new ConcurrentHashMap<>();
  private volatile String defaultSchema;

  List<String> getPrimaryKeys(String schema, String table) {
    if (!StringUtils.hasText(table)) {
      return List.of();
    }
    String resolvedSchema = resolveSchema(schema);
    if (!StringUtils.hasText(resolvedSchema)) {
      return List.of();
    }

    String cacheKey = resolvedSchema + "." + table;
    return cache.computeIfAbsent(cacheKey, key -> loadPrimaryKeys(resolvedSchema, table));
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

  private List<String> loadPrimaryKeys(String schema, String table) {
    List<String> columns =
        jdbcTemplate.query(PRIMARY_KEY_SQL, (rs, rowNum) -> rs.getString(1), schema, table);
    return List.copyOf(columns);
  }
}
