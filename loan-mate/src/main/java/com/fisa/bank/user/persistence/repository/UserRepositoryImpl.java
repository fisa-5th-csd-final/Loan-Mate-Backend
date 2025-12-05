package com.fisa.bank.user.persistence.repository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fisa.bank.user.application.model.ServiceUser;
import com.fisa.bank.user.application.repository.UserRepository;
import com.fisa.bank.user.persistence.UserMapper;
import com.fisa.bank.user.persistence.entity.UserEntity;
import com.fisa.bank.user.persistence.entity.id.UserId;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository jpaRepo;
  private final UserMapper mapper;

  @Override
  public Optional<ServiceUser> findById(Long id) {
    return jpaRepo.findById(UserId.of(id)).map(mapper::toDomain);
  }

  @Override
  public ServiceUser save(ServiceUser user) {
    UserEntity saved = jpaRepo.save(mapper.toEntity(user));
    return mapper.toDomain(saved);
  }

  @Override
  public List<ServiceUser> findAllByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    List<UserId> userIds = ids.stream().map(UserId::of).toList();
    return jpaRepo.findAllById(userIds).stream().map(mapper::toDomain).toList();
  }
}
