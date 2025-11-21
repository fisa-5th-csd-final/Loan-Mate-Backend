package com.fisa.bank.user.application.dto;

public record LoginResponse(String accessToken, String refreshToken, Long userId) {}
