package com.fisa.bank.user.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.user.persistence.entity.UserAuthEntity;
import com.fisa.bank.user.persistence.entity.id.UserAuthId;

public interface JpaUserAuthRepository extends JpaRepository<UserAuthEntity, UserAuthId> {
  Optional<UserAuthEntity> findByCoreBankingUserId(Long coreBankingUserId);

  Optional<UserAuthEntity> findByServiceUserId(Long serviceUserId);
}
