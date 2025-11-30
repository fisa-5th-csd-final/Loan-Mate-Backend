package com.fisa.bank.user.persistence;

import org.springframework.stereotype.Component;

import com.fisa.bank.user.application.model.ServiceUser;
import com.fisa.bank.user.persistence.entity.UserEntity;
import com.fisa.bank.user.persistence.entity.id.UserId;

@Component
public class UserMapper {

  public ServiceUser toDomain(UserEntity entity) {
    if (entity == null) {
      return null;
    }

    return new ServiceUser(
        entity.getUserId() != null ? entity.getUserId().getValue() : null, // UserId → Long
        entity.getName(),
        entity.getAddress(),
        entity.getJob(),
        entity.getBirthday(),
        entity.getCreditLevel(),
        entity.getCustomerLevel());
  }

  public UserEntity toEntity(ServiceUser domain) {
    if (domain == null) {
      return null;
    }

    return UserEntity.builder()
        .userId(domain.getUserId() != null ? UserId.of(domain.getUserId()) : null) // Long → UserId
        .name(domain.getName())
        .address(domain.getAddress())
        .job(domain.getJob())
        .creditLevel(domain.getCreditLevel())
        .customerLevel(domain.getCustomerLevel())
        .build();
  }
}
