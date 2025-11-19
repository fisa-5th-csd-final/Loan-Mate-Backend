package com.fisa.bank.infrastructure.cdc;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@Getter
@Setter
@ConfigurationProperties(prefix = "cdc.replication")
public class CdcReplicationProperties {

  /** 스키마명이 주어지지 않았을 때 사용할 기본 스키마. */
  private String defaultSchema;

  /** 원본 스키마명 → 대상 스키마명 매핑. */
  private Map<String, String> schemaMapping = new HashMap<>();

  /** 테이블별 허용 컬럼 목록. */
  private Map<String, List<String>> tableColumnFilters = new HashMap<>();

  public void setSchemaMapping(Map<String, String> schemaMapping) {
    this.schemaMapping = normalizeSchemaMapping(schemaMapping);
  }

  public void setTableColumnFilters(Map<String, List<String>> tableColumnFilters) {
    this.tableColumnFilters = normalizeTableColumnFilters(tableColumnFilters);
  }

  public String mapSchema(String sourceSchema) {
    if (StringUtils.hasText(sourceSchema)) {
      String mapped = schemaMapping.get(sourceSchema.toLowerCase(Locale.ROOT));
      if (StringUtils.hasText(mapped)) {
        return mapped;
      }
    }
    if (StringUtils.hasText(defaultSchema)) {
      return defaultSchema;
    }
    return sourceSchema;
  }

  public List<String> getAllowedColumns(String schema, String table) {
    if (!StringUtils.hasText(table)) {
      return List.of();
    }
    String normalizedTable = table.toLowerCase(Locale.ROOT);
    String schemaQualifiedKey =
        StringUtils.hasText(schema)
            ? schema.toLowerCase(Locale.ROOT) + "." + normalizedTable
            : normalizedTable;

    List<String> columns =
        tableColumnFilters.getOrDefault(
            schemaQualifiedKey, tableColumnFilters.get(normalizedTable));
    if (columns == null) {
      return List.of();
    }
    return columns;
  }

  private Map<String, String> normalizeSchemaMapping(Map<String, String> input) {
    Map<String, String> normalized = new HashMap<>();
    if (input == null) {
      return normalized;
    }
    input.forEach(
        (key, value) -> {
          if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
            normalized.put(key.toLowerCase(Locale.ROOT), value);
          }
        });
    return normalized;
  }

  private Map<String, List<String>> normalizeTableColumnFilters(Map<String, List<String>> source) {
    Map<String, List<String>> normalized = new HashMap<>();
    if (source == null) {
      return normalized;
    }
    source.forEach(
        (key, value) -> {
          if (!StringUtils.hasText(key) || value == null) {
            return;
          }
          String normalizedKey = key.toLowerCase(Locale.ROOT);
          List<String> normalizedColumns = new ArrayList<>();
          for (String column : value) {
            if (StringUtils.hasText(column)) {
              normalizedColumns.add(column.toLowerCase(Locale.ROOT));
            }
          }
          normalized.put(normalizedKey, List.copyOf(normalizedColumns));
        });
    return normalized;
  }
}
