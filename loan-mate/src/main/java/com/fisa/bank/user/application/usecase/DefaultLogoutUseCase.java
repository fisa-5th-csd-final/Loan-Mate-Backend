package com.fisa.bank.user.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fisa.bank.common.application.util.RequesterInfo;
import com.fisa.bank.user.application.repository.RefreshTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DefaultLogoutUseCase implements LogoutUseCase {
  private final RefreshTokenRepository refreshTokenRepository;
  private final RequesterInfo requesterInfo;

  @Override
  public void execute() {
    log.info("로그아웃 요청");

    // SecurityContext 에서 현재 인증된 유저 ID 가져오기
    Long userId = requesterInfo.getServiceUserId();

    if (userId == null) {
      log.warn("로그아웃 실패: 인증된 사용자 정보 없음");
      return;
    }

    log.info("로그아웃 진행 userId={}", userId);

    // 해당 유저의 Refresh Token 삭제
    String refreshToken = refreshTokenRepository.findByUserId(userId).orElse(null);
    if (refreshToken == null) {
      log.info("삭제할 RefreshToken 없음");
      return;
    }

    refreshTokenRepository.deleteByToken(refreshToken);

    log.info("유저 {}의 RefreshToken 삭제 완료", userId);
  }
}
