package com.fisa.bank.common.util;

import org.springframework.http.ResponseCookie;

public interface CookieUtil {
  ResponseCookie createHttpOnlyCookie(String name, String value, int maxAgeSeconds);

  ResponseCookie deleteCookie(String name);
}
