package com.fisa.bank.accountbook.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.persistence.entity.ManualLedgerEntity;

public interface JpaManualLedgerRepository extends JpaRepository<ManualLedgerEntity, Long> {

  List<ManualLedgerEntity> findAllByServiceUserIdOrderBySavedAtDesc(Long serviceUserId);

  List<ManualLedgerEntity> findAllByServiceUserIdAndTypeOrderBySavedAtDesc(
      Long serviceUserId, ManualLedgerType type);
}
