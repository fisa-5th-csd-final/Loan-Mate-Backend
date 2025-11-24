package com.fisa.bank.common.config.security;

import lombok.Getter;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * 서비스 사용자 인증 객체
 *
 * <p>JWT 토큰 기반 인증에서 사용되며, 서비스 사용자 ID를 포함합니다.
 */
@Getter
public class ServiceUserAuthentication extends AbstractAuthenticationToken {

  private final Long userId;

  public ServiceUserAuthentication(
      Long userId, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.userId = userId;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getPrincipal() {
    return userId;
  }
}
