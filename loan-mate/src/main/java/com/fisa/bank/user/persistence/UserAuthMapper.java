package com.fisa.bank.user.persistence;

import org.springframework.stereotype.Component;

import com.fisa.bank.user.application.model.UserAuth;
import com.fisa.bank.user.persistence.entity.UserAuthEntity;
import com.fisa.bank.user.persistence.entity.id.UserAuthId;

@Component
public class UserAuthMapper {

  /** Domain → Entity */
  public UserAuthEntity toEntity(UserAuth domain) {
    return UserAuthEntity.builder()
        .id(domain.getId() != null ? UserAuthId.of(domain.getId()) : null)
        .coreBankingUserId(domain.getCoreBankingUserId())
        .serviceUserId(domain.getServiceUserId())
        .build();
  }

  /** Entity → Domain */
  public UserAuth toDomain(UserAuthEntity entity) {
    return new UserAuth(
        entity.getId() != null ? entity.getId().getValue() : null,
        entity.getCoreBankingUserId(),
        entity.getServiceUserId());
  }
}
