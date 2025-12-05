package com.fisa.bank.user.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fisa.bank.user.persistence.entity.UserEntity;
import com.fisa.bank.user.persistence.entity.id.UserId;

public interface JpaUserRepository extends JpaRepository<UserEntity, UserId> {

  @Query(
      value = "select user_id from user_service where birthday between :start and :end",
      nativeQuery = true)
  List<Long> findIdsByBirthdayBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
