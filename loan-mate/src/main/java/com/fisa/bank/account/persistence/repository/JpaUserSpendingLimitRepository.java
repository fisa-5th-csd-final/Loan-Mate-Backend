package com.fisa.bank.account.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.account.persistence.entity.UserSpendingLimitEntity;

public interface JpaUserSpendingLimitRepository
    extends JpaRepository<UserSpendingLimitEntity, Long> {

  Optional<UserSpendingLimitEntity> findByServiceUserId(Long serviceUserId);
}
