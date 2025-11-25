package com.fisa.bank.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie createHttpOnlyCookie(
      String name, String value, int maxAgeSeconds, boolean secure) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(secure);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);
    return cookie;
  }

  public static Cookie deleteCookie(String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    return cookie;
  }
}
