package com.fisa.bank.user.application.dto;

import org.springframework.stereotype.Component;

import com.fisa.bank.user.application.model.ServiceUser;

@Component
public class UserApplicationMapper {

  public ServiceUser toDomain(UserInfoResponse info) {
    return new ServiceUser(
        null, // 서비스 UserId는 아직 없음
        info.name(),
        info.address(),
        info.job(),
        info.birthday(),
        info.creditLevel(),
        info.customerLevel());
  }
}
