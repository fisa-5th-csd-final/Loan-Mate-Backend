package com.fisa.bank.accountbook.persistence.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.persistence.entity.ManualLedgerEntity;

public interface JpaManualLedgerRepository extends JpaRepository<ManualLedgerEntity, Long> {

  List<ManualLedgerEntity> findAllByServiceUserIdOrderBySavedAtDesc(Long serviceUserId);

  List<ManualLedgerEntity> findAllByServiceUserIdAndTypeOrderBySavedAtDesc(
      Long serviceUserId, ManualLedgerType type);

  @Query(
      """
        SELECT COALESCE(SUM(m.amount), 0)
        FROM ManualLedgerEntity m
        WHERE m.serviceUserId = :serviceUserId
          AND m.type = :type
          AND m.savedAt >= :startDate
          AND m.savedAt < :endDate
      """)
  BigDecimal sumAmountByUserIdAndTypeBetween(
      @Param("serviceUserId") Long serviceUserId,
      @Param("type") ManualLedgerType type,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);
}
