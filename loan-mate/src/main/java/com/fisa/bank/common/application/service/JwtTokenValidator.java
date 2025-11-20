package com.fisa.bank.common.application.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class JwtTokenValidator {

  @Value("${jwt.issuer-uri}")
  private String issuerUri;

  private PublicKey publicKey;

  private final WebClient webClient = WebClient.create();

  @PostConstruct
  public void init() {
    try {
      if (issuerUri == null || issuerUri.isBlank()) {
        throw new RuntimeException("issuerUri 설정 없음");
      }

      // openid-configuration 조회 → jwks_uri 얻기
      String jwksUri = fetchJwksUri(issuerUri);

      // jwks_uri로 JWKS 조회 → public key 추출
      this.publicKey = loadPublicKeyFromJwks(jwksUri);

    } catch (Exception e) {
      throw new RuntimeException("JWT key 초기화 실패", e);
    }
  }

  /** JWT 검증 */
  public Claims validateToken(String token) {
    if (publicKey == null) {
      throw new JwtException("Public key not configured");
    }

    try {
      return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
    } catch (ExpiredJwtException e) {
      log.debug("토큰 만료: {}", e.getMessage());
      throw e;
    } catch (SignatureException e) {
      log.debug("서명 불일치: {}", e.getMessage());
      throw e;
    } catch (MalformedJwtException e) {
      log.debug("JWT 형식 오류: {}", e.getMessage());
      throw e;
    } catch (UnsupportedJwtException e) {
      log.debug("지원하지 않는 JWT: {}", e.getMessage());
      throw e;
    }
  }

  public boolean isValid(String token) {
    try {
      validateToken(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  // openid-configuration에서 jwks_uri 획득
  private String fetchJwksUri(String issuer) {
    Map<String, Object> config =
        webClient
            .get()
            .uri(issuer + "/.well-known/openid-configuration")
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    if (config == null || config.get("jwks_uri") == null) {
      throw new RuntimeException("openid-configuration에서 jwks_uri를 가져올 수 없음");
    }

    return config.get("jwks_uri").toString();
  }

  // JWKS에서 RSA 공개키 하나 읽기
  @SuppressWarnings("unchecked")
  private PublicKey loadPublicKeyFromJwks(String jwksUri) {
    try {
      Map<String, Object> jwks =
          webClient.get().uri(jwksUri).retrieve().bodyToMono(Map.class).block();

      if (jwks == null || !jwks.containsKey("keys")) {
        throw new RuntimeException("JWKS 응답이 유효하지 않음");
      }

      List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

      Map<String, Object> rsaKey =
          keys.stream()
              .filter(k -> "RSA".equals(k.get("kty")))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("JWKS에 RSA key가 없음"));

      String n = (String) rsaKey.get("n");
      String e = (String) rsaKey.get("e");

      BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
      BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));

      return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));

    } catch (Exception ex) {
      throw new RuntimeException("JWKS에서 공개키 로딩 실패", ex);
    }
  }
}
