package com.fisa.bank.common.util;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class CookieUtilProd implements CookieUtil {

  @Override
  public ResponseCookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true) // prod는 https만 사용
        .sameSite("None") // 크로스 도메인 필수
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
  }

  @Override
  public ResponseCookie deleteCookie(String name) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(true)
        .sameSite("None")
        .path("/")
        .maxAge(0)
        .build();
  }
}
