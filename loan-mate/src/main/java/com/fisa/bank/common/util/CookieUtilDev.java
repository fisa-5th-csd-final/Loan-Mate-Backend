package com.fisa.bank.common.util;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "local"})
public class CookieUtilDev implements CookieUtil {

  @Override
  public ResponseCookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(false) // dev는 secure=false
        .sameSite("Lax")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
  }

  @Override
  public ResponseCookie deleteCookie(String name) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .secure(false)
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();
  }
}
