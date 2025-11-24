package com.fisa.bank.user.application.usecase;

import com.fisa.bank.user.application.dto.RefreshTokenResponse;

public interface UpdateTokenUseCase {
  RefreshTokenResponse execute(String refreshToken);
}
