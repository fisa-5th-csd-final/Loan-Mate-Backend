package com.fisa.bank.user.application.usecase;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.user.application.dto.UserApplicationMapper;
import com.fisa.bank.user.application.dto.UserInfoResponse;
import com.fisa.bank.user.application.model.User;
import com.fisa.bank.user.application.model.UserAuth;
import com.fisa.bank.user.application.repository.UserAuthRepository;
import com.fisa.bank.user.application.repository.UserRepository;
import com.fisa.bank.user.application.service.LoginResult;

@Service
@RequiredArgsConstructor
@Transactional
public class DefaultSyncCoreBankUserUseCase implements SyncCoreBankUserUseCase {
  private final UserRepository userRepository;
  private final UserAuthRepository userAuthRepository;
  private final UserApplicationMapper mapper;

  @Override
  public LoginResult sync(UserInfoResponse info) {

    // coreBankingUserId로 매핑 존재 여부 확인
    var authOpt = userAuthRepository.findByCoreBankingUserId(info.userId());

    // 이미 매핑된 기존 사용자
    if (authOpt.isPresent()) {

      Long userId = authOpt.get().getServiceUserId();

      User existingUser =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new IllegalStateException("UserAuth는 있는데 User가 없음"));

      // 기존 도메인 객체 값 변경
      existingUser.updateProfile(info.name(), info.address(), info.job());

      existingUser.updateCreditRating(info.creditLevel());
      existingUser.upgradeCustomerLevel(info.customerLevel());

      userRepository.save(existingUser);

      return LoginResult.LOGIN;
    }

    // 신규 사용자 등록

    // DTO → Domain 변환
    User newUser = mapper.toDomain(info);

    // User 저장 → PK(Long) 생성
    newUser = userRepository.save(newUser);

    // 사용자 매핑 생성
    userAuthRepository.save(
        new UserAuth(
            null, // authId 자동
            newUser.getUserId(), // 서비스 user PK
            info.userId() // 코어뱅킹 user PK
            ));

    return LoginResult.REGISTER;
  }
}
