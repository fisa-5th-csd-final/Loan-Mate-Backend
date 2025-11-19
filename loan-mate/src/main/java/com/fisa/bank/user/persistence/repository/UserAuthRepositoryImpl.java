package com.fisa.bank.user.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.user.application.model.UserAuth;
import com.fisa.bank.user.application.repository.UserAuthRepository;
import com.fisa.bank.user.persistence.UserAuthMapper;
import com.fisa.bank.user.persistence.entity.UserAuthEntity;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {
  private final JpaUserAuthRepository jpaRepo;
  private final UserAuthMapper mapper;

  @Override
  public Optional<UserAuth> findByCoreBankingUserId(Long coreBankingUserId) {
    return jpaRepo.findByCoreBankingUserId(coreBankingUserId).map(mapper::toDomain);
  }

  @Override
  public Optional<UserAuth> findByServiceUserId(Long serviceUserId) {
    return jpaRepo.findByServiceUserId(serviceUserId).map(mapper::toDomain);
  }

  @Override
  public UserAuth save(UserAuth userAuth) {
    UserAuthEntity saved = jpaRepo.save(mapper.toEntity(userAuth));
    return mapper.toDomain(saved);
  }
}
