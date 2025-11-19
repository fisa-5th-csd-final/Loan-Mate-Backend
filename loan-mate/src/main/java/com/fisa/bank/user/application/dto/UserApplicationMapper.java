package com.fisa.bank.user.application.dto;

import org.springframework.stereotype.Component;

import com.fisa.bank.user.application.model.User;

@Component
public class UserApplicationMapper {

  public User toDomain(UserInfoResponse info) {
    return new User(
        null, // 서비스 UserId는 아직 없음
        info.name(),
        info.address(),
        info.job(),
        info.creditLevel(),
        info.customerLevel());
  }
}
