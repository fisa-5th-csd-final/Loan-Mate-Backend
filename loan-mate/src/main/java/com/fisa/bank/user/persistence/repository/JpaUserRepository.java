package com.fisa.bank.user.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fisa.bank.user.persistence.entity.UserEntity;
import com.fisa.bank.user.persistence.entity.id.UserId;

public interface JpaUserRepository extends JpaRepository<UserEntity, UserId> {}
