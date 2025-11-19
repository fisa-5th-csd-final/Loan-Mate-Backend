package com.fisa.bank.infrastructure.cdc;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fisa.bank.cdc.listener.event.CdcEvent;
import com.fisa.bank.cdc.listener.event.CdcEventConsumer;

/**
 * Kafka 로부터 들어온 CDC 이벤트를 읽고, 수신한 로그 내용을 그대로 로컬 MySQL 테이블에 반영한다.
 *
 * <p>대상 테이블과 PK 는 CDC 로그에 포함된 스키마 정보를 기준으로 동적으로 계산한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "cdc",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class TableReplicatingCdcConsumer implements CdcEventConsumer {

  private final JdbcTemplate jdbcTemplate;
  private final TablePrimaryKeyMetadataProvider metadataProvider;
  private final TableColumnMetadataProvider columnMetadataProvider;
  private final TableColumnValueConverter columnValueConverter;
  private final CdcReplicationProperties replicationProperties;

  @Override
  public boolean supports(CdcEvent event) {
    return event != null && StringUtils.hasText(event.table());
  }

  @Override
  public void handle(CdcEvent event) {
    Operation operation = Operation.from(event.operation());
    if (operation == Operation.UNKNOWN) {
      log.warn("지원하지 않는 CDC operation 이 수신됐습니다. op={} event={}", event.operation(), event);
      return;
    }

    TableRef table = TableRef.from(event, replicationProperties::mapSchema);
    if (!StringUtils.hasText(table.table())) {
      log.warn("테이블 정보가 없는 CDC 이벤트는 처리할 수 없습니다. event={}", event);
      return;
    }

    log.debug("CDC 이벤트를 수신했습니다. (scheme = {}, table = {})", event.database(), event.table());
    Map<String, Object> before =
        filterColumns(table, normalizeByMetadata(table, toMap(event.before())));
    Map<String, Object> after =
        filterColumns(table, normalizeByMetadata(table, toMap(event.after())));
    try {
      switch (operation) {
        case CREATE, READ -> upsert(table, after, operation);
        case UPDATE -> upsert(table, after, operation);
        case DELETE -> delete(table, before);
        default -> {}
      }
    } catch (DataAccessException ex) {
      log.error(
          "CDC 이벤트를 DB 에 반영하는 데 실패했습니다. table={} op={} before={} after={}",
          String.format("%s.%s", table.schema(), table.table()),
          operation,
          before,
          after,
          ex);
      throw ex;
    }
  }

  private void upsert(TableRef table, Map<String, Object> payload, Operation operation) {
    if (payload.isEmpty()) {
      log.warn("{}", buildMissingPayloadMessage(table, operation));
      return;
    }

    List<String> columns = new ArrayList<>(payload.keySet());
    List<Object> values = columns.stream().map(payload::get).toList();

    String insertColumns =
        columns.stream().map(this::quoteIdentifier).collect(Collectors.joining(", "));
    String placeholders = columns.stream().map(column -> "?").collect(Collectors.joining(", "));
    String updateSetClause =
        columns.stream()
            .map(
                column ->
                    quoteIdentifier(column)
                        + " = VALUES("
                        + quoteIdentifier(column)
                        + ")")
            .collect(Collectors.joining(", "));

    String sql =
        "INSERT INTO "
            + table.qualifiedName()
            + " ("
            + insertColumns
            + ") VALUES ("
            + placeholders
            + ") ON DUPLICATE KEY UPDATE "
            + updateSetClause;

    jdbcTemplate.update(sql, values.toArray());
    log.debug("CDC {} 이벤트를 {} 테이블에 반영했습니다.", operation, String.format("%s.%s", table.schema, table.table));
  }

  private void delete(TableRef table, Map<String, Object> before) {
    if (before.isEmpty()) {
      log.warn("{}", buildMissingPayloadMessage(table, Operation.DELETE));
      return;
    }

    List<String> primaryKeys =
        metadataProvider.getPrimaryKeys(table.schema(), table.table());
    if (primaryKeys.isEmpty()) {
      log.warn(
          "PK 정보를 찾을 수 없어 CDC 삭제 이벤트를 건너뜁니다. table={}", table.qualifiedName());
      return;
    }

    List<String> clauses = new ArrayList<>(primaryKeys.size());
    List<Object> parameters = new ArrayList<>(primaryKeys.size());

    for (String pk : primaryKeys) {
      if (!before.containsKey(pk)) {
        log.warn(
            "삭제 이벤트에 PK {} 컬럼 값이 포함되지 않아 CDC 이벤트를 건너뜁니다. table={}",
            pk,
            table.qualifiedName());
        return;
      }
      clauses.add(quoteIdentifier(pk) + " = ?");
      parameters.add(before.get(pk));
    }

    String whereClause = String.join(" AND ", clauses);
    String sql = "DELETE FROM " + table.qualifiedName() + " WHERE " + whereClause;
    jdbcTemplate.update(sql, parameters.toArray());
    log.debug("CDC DELETE 이벤트를 {} 테이블에 반영했습니다.", table.qualifiedName());
  }

  private String buildMissingPayloadMessage(TableRef table, Operation operation) {
    return String.format(
        "CDC %s 이벤트에 반영할 데이터가 없어 건너뜁니다. table=%s",
        operation, table.qualifiedName());
  }

  private Map<String, Object> normalizeByMetadata(
      TableRef table, Map<String, Object> originalValues) {
    if (originalValues.isEmpty()) {
      return originalValues;
    }
    Map<String, TableColumnMetadataProvider.ColumnMetadata> metadata =
        columnMetadataProvider.getColumnMetadata(table.schema(), table.table());
    if (metadata.isEmpty()) {
      return originalValues;
    }

    Map<String, Object> normalized = new LinkedHashMap<>(originalValues.size());
    for (Entry<String, Object> entry : originalValues.entrySet()) {
      TableColumnMetadataProvider.ColumnMetadata columnMetadata = metadata.get(entry.getKey());
      normalized.put(entry.getKey(), columnValueConverter.convert(columnMetadata, entry.getValue()));
    }
    return normalized;
  }

  private Map<String, Object> filterColumns(TableRef table, Map<String, Object> values) {
    List<String> allowedColumns =
        replicationProperties.getAllowedColumns(table.schema(), table.table());
    if (values.isEmpty() || allowedColumns.isEmpty()) {
      return values;
    }
    Map<String, Object> filtered = new LinkedHashMap<>();
    for (Entry<String, Object> entry : values.entrySet()) {
      String normalizedKey = entry.getKey().toLowerCase(Locale.ROOT);
      if (allowedColumns.contains(normalizedKey)) {
        filtered.put(entry.getKey(), entry.getValue());
      }
    }
    return filtered;
  }

  private Map<String, Object> toMap(JsonNode node) {
    if (node == null || node.isNull()) {
      return Map.of();
    }
    Map<String, Object> values = new LinkedHashMap<>();
    Iterator<Entry<String, JsonNode>> fields = node.fields();
    while (fields.hasNext()) {
      Entry<String, JsonNode> field = fields.next();
      values.put(field.getKey(), toValue(field.getValue()));
    }
    return values;
  }

  private Object toValue(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }

    if (node.isBoolean()) {
      return node.booleanValue();
    }
    if (node.isIntegralNumber()) {
      if (node.canConvertToLong()) {
        return node.longValue();
      }
      return node.bigIntegerValue();
    }
    if (node.isFloatingPointNumber()) {
      return node.decimalValue();
    }
    if (node.isTextual()) {
      return node.textValue();
    }
    if (node.isBinary()) {
      try {
        return node.binaryValue();
      } catch (IOException e) {
        log.warn("CDC payload 의 binary 값을 읽어올 수 없어 문자열로 대체합니다.", e);
        return node.asText();
      }
    }
    if (node.isArray() || node.isObject()) {
      // JSON 타입은 문자열로 저장해서 그대로 복제한다.
      return node.toString();
    }
    return node.asText();
  }

  private String quoteIdentifier(String identifier) {
    return "`" + identifier + "`";
  }

  private enum Operation {
    CREATE("c"),
    READ("r"),
    UPDATE("u"),
    DELETE("d"),
    UNKNOWN("");

    private final String code;

    Operation(String code) {
      this.code = code;
    }

    static Operation from(String code) {
      if (!StringUtils.hasText(code)) {
        return UNKNOWN;
      }
      for (Operation operation : values()) {
        if (operation.code.equalsIgnoreCase(code)) {
          return operation;
        }
      }
      return UNKNOWN;
    }
  }

  private record TableRef(String schema, String table) {

    static TableRef from(CdcEvent event, Function<String, String> schemaMapper) {
      String schema = event.database();
      String tableName = event.table();
      if (StringUtils.hasText(tableName) && tableName.contains(".")) {
        String[] splitted = tableName.split("\\.", 2);
        if (!StringUtils.hasText(schema)) {
          schema = splitted[0];
        }
        tableName = splitted[1];
      }
      if (schemaMapper != null) {
        schema = schemaMapper.apply(schema);
      }
      return new TableRef(schema, tableName);
    }

    String qualifiedName() {
      if (StringUtils.hasText(schema)) {
        return quote(schema) + "." + quote(table);
      }
      return quote(table);
    }

    private String quote(String name) {
      return "`" + name + "`";
    }
  }
}
