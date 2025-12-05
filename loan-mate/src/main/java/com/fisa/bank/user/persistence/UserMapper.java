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
        entity.getCustomerLevel(),
        entity.getIncome());
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
        .birthday(domain.getBirthday())
        .creditLevel(domain.getCreditLevel())
        .customerLevel(domain.getCustomerLevel())
        .income(domain.getIncome())
        .build();
  }
}
