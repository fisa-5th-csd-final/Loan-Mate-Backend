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
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class JwtTokenValidator {

  @Value("${jwt.issuer-uri}")
  private String issuerUri;

  private final WebClient webClient = WebClient.create();
  private final ObjectMapper mapper = new ObjectMapper();

  /** kid → PublicKey 매핑 */
  private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

  /** JWKS URL */
  private String jwksUri;

  @PostConstruct
  public void init() {
    if (issuerUri == null || issuerUri.isBlank()) {
      throw new RuntimeException("issuerUri 설정 없음");
    }

    // openid-config → jwks_uri
    this.jwksUri = fetchJwksUri(issuerUri);

    // 첫 로딩
    reloadJwks();
  }

  /** JWT 검증 */
  public Claims validateToken(String token) {
    try {
      String kid = extractKid(token);

      PublicKey key = keyCache.get(kid);
      if (key == null) {
        log.warn("kid={} 키 없음. JWKS 재로딩 시도.", kid);
        reloadJwks();
        key = keyCache.get(kid);
      }

      if (key == null) {
        throw new JwtException("JWKS에서 kid=" + kid + " 키를 찾을 수 없습니다.");
      }

      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

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

  /** JWKS 재로드 */
  @SuppressWarnings("unchecked")
  private synchronized void reloadJwks() {
    try {
      Map<String, Object> jwks =
          webClient.get().uri(jwksUri).retrieve().bodyToMono(Map.class).block();

      List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

      keyCache.clear();

      for (Map<String, Object> k : keys) {
        if (!"RSA".equals(k.get("kty"))) continue;

        String kid = (String) k.get("kid");
        String n = (String) k.get("n");
        String e = (String) k.get("e");

        PublicKey publicKey = createRsaPublicKey(n, e);
        keyCache.put(kid, publicKey);
      }

    } catch (Exception e) {
      throw new RuntimeException("JWKS 키 재로딩 실패", e);
    }
  }

  /** openid-config 에서 jwks_uri 얻기 */
  private String fetchJwksUri(String issuer) {
    Map<String, Object> config =
        webClient
            .get()
            .uri(issuer + "/.well-known/openid-configuration")
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    if (config == null || config.get("jwks_uri") == null) {
      throw new RuntimeException("jwks_uri를 openid-config에서 가져올 수 없음");
    }

    return config.get("jwks_uri").toString();
  }

  /** JWT header에서 kid 추출 */
  private String extractKid(String token) {
    try {
      String headerPart = token.split("\\.")[0];
      byte[] decoded = Base64.getUrlDecoder().decode(headerPart);
      Map<String, Object> header = mapper.readValue(decoded, Map.class);
      return (String) header.get("kid");
    } catch (Exception e) {
      throw new JwtException("JWT 헤더에서 kid 추출 실패", e);
    }
  }

  /** RSA 공개키 생성 */
  private PublicKey createRsaPublicKey(String n, String e) throws Exception {
    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
    return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
  }
}
