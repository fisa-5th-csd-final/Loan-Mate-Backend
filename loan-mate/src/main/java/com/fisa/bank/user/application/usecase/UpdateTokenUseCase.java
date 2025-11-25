package com.fisa.bank.user.application.usecase;

import com.fisa.bank.user.application.dto.TokenPair;

public interface UpdateTokenUseCase {
  TokenPair execute(String refreshToken);
}
