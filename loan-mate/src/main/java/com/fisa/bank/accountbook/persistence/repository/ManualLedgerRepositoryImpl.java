package com.fisa.bank.accountbook.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.accountbook.application.model.ManualLedgerEntry;
import com.fisa.bank.accountbook.application.model.ManualLedgerType;
import com.fisa.bank.accountbook.application.repository.ManualLedgerRepository;
import com.fisa.bank.accountbook.persistence.ManualLedgerMapper;
import com.fisa.bank.accountbook.persistence.entity.ManualLedgerEntity;

@Repository
@RequiredArgsConstructor
public class ManualLedgerRepositoryImpl implements ManualLedgerRepository {

  private final JpaManualLedgerRepository jpaManualLedgerRepository;
  private final ManualLedgerMapper mapper;

  @Override
  public ManualLedgerEntry save(ManualLedgerEntry entry) {
    ManualLedgerEntity saved = jpaManualLedgerRepository.save(mapper.toEntity(entry));
    return mapper.toDomain(saved);
  }

  @Override
  public List<ManualLedgerEntry> findByUserId(Long serviceUserId) {
    return jpaManualLedgerRepository
        .findAllByServiceUserIdOrderBySavedAtDesc(serviceUserId)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<ManualLedgerEntry> findByUserIdAndType(Long serviceUserId, ManualLedgerType type) {
    return jpaManualLedgerRepository
        .findAllByServiceUserIdAndTypeOrderBySavedAtDesc(serviceUserId, type)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public Optional<ManualLedgerEntry> findById(Long id) {
    return jpaManualLedgerRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public void deleteById(Long id) {
    jpaManualLedgerRepository.deleteById(id);
  }
}
