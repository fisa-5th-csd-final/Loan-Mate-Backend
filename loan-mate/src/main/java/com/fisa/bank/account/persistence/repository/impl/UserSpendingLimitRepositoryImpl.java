package com.fisa.bank.account.persistence.repository.impl;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.account.application.model.spending.UserSpendingLimit;
import com.fisa.bank.account.application.repository.UserSpendingLimitRepository;
import com.fisa.bank.account.persistence.entity.UserSpendingLimitEntity;
import com.fisa.bank.account.persistence.mapper.UserSpendingLimitMapper;
import com.fisa.bank.account.persistence.repository.jpa.JpaUserSpendingLimitRepository;

@Repository
@RequiredArgsConstructor
public class UserSpendingLimitRepositoryImpl implements UserSpendingLimitRepository {

  private final JpaUserSpendingLimitRepository jpaRepository;
  private final UserSpendingLimitMapper mapper;

  @Override
  public UserSpendingLimit save(UserSpendingLimit limit) {
    UserSpendingLimitEntity entity =
        jpaRepository
            .findByServiceUserId(limit.serviceUserId())
            .map(
                existing -> {
                  existing.updateLimits(limit.limits());
                  return existing;
                })
            .orElseGet(() -> mapper.toEntity(limit));

    return mapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  public Optional<UserSpendingLimit> findByServiceUserId(Long serviceUserId) {
    return jpaRepository.findByServiceUserId(serviceUserId).map(mapper::toDomain);
  }
}
