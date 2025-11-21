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
import java.time.Duration;
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

  /** jwks-uri 를 발급받는 외부의 서버 uri */
  @Value("${jwt.issuer-uri}")
  private String issuerUri;

  /** yml 에서 직접 정의한 JWKS URI (있으면 무조건 우선 사용) */
  @Value("${jwt.jwk-set-uri:#{null}}")
  private String jwkSetUri;

  private final WebClient webClient = WebClient.create();
  private final ObjectMapper mapper = new ObjectMapper();

  /** kid → PublicKey 캐시 */
  private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

  /** 실제 사용 중인 JWKS URI */
  private volatile String jwksUri;

  /** 마지막 JWKS 성공 로딩 시간 */
  private volatile long lastJwksLoadedAt = 0L;

  /** JWKS 재로딩 간격 (100분) */
  private static final long JWKS_RELOAD_INTERVAL = Duration.ofMinutes(100).toMillis();

  @PostConstruct
  public void init() {
    if (issuerUri == null || issuerUri.isBlank()) {
      throw new IllegalStateException("jwt.issuer-uri 설정이 필요합니다.");
    }

    // yml에 명시된 JWKS URI가 있으면 최우선 사용
    if (jwkSetUri != null && !jwkSetUri.isBlank()) {
      this.jwksUri = jwkSetUri;
      log.info("미리 정의된 JWKS URI 사용: {}", jwksUri);
    } else {
      // 없다면 issuer 기반으로 lazy 로딩
      log.info("JWKS URI 는 issuer 기반으로 lazy 로딩 issuer={}", issuerUri);
    }
  }

  // JWT 검증
  public Claims validateToken(String token) {
    try {
      String kid = extractKid(token);

      // 키 캐시에 없는 경우 또는 재로딩 시간이 된 경우
      PublicKey key = keyCache.get(kid);
      if (key == null || needReload()) {
        tryReloadJwks();
        key = keyCache.get(kid);
      }

      if (key == null) {
        throw new JwtException("kid=" + kid + " 에 해당하는 공개키 없음");
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

  /** 내부 메서드 */

  /** JWT 헤더에서 kid 추출 */
  private String extractKid(String token) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length != 3) {
        throw new MalformedJwtException("JWT 토큰은 세 부분으로 구성되어야 합니다.");
      }
      String headerPart = parts[0];
      byte[] decoded = Base64.getUrlDecoder().decode(headerPart);
      Map<String, Object> header = mapper.readValue(decoded, Map.class);

      Object kid = header.get("kid");
      if (kid == null) {
        throw new JwtException("JWT 헤더에 kid가 없습니다.");
      }

      return kid.toString();

    } catch (IllegalArgumentException | com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new JwtException("JWT 헤더에서 kid 추출 실패", e);
    }
  }

  /** 재로딩 기간 지났는지 */
  private boolean needReload() {
    long now = System.currentTimeMillis();
    return (now - lastJwksLoadedAt) >= JWKS_RELOAD_INTERVAL;
  }

  /** JWKS 로딩 */
  private synchronized void tryReloadJwks() {
    try {
      // config 의 jwk-set-uri 우선 사용
      if (jwkSetUri != null && !jwkSetUri.isBlank()) {
        log.debug("JWKS 로딩 시도: configured jwk-set-uri={}", jwkSetUri);
        Map<String, PublicKey> refreshed = loadAllKeysFromJwks(jwkSetUri);
        applyNewKeys(refreshed);
        return;
      }

      // issuer 기반으로 jwks_uri 조회
      log.warn("configured jwk-set-uri 없음. issuer에서 jwks_uri 획득 시도…");

      this.jwksUri = fetchJwksUriFromIssuer(issuerUri);
      log.info("issuer 기반 최신 jwks_uri 획득: {}", jwksUri);

      Map<String, PublicKey> refreshed = loadAllKeysFromJwks(jwksUri);
      applyNewKeys(refreshed);

    } catch (Exception ex) {
      log.error("JWKS 로딩 실패. 기존 keyCache 유지. reason={}", ex.getMessage());
    }
  }

  /** openid-configuration에서 jwks_uri 획득 */
  private String fetchJwksUriFromIssuer(String issuer) {
    Map<String, Object> config =
        webClient
            .get()
            .uri(issuer + "/.well-known/openid-configuration")
            .retrieve()
            .bodyToMono(Map.class)
            .block();

    if (config == null || config.get("jwks_uri") == null) {
      throw new RuntimeException("issuer에서 jwks_uri 가져오기 실패");
    }

    return config.get("jwks_uri").toString();
  }

  /** JWKS 전체 키 로딩 */
  @SuppressWarnings("unchecked")
  private Map<String, PublicKey> loadAllKeysFromJwks(String uri) throws Exception {
    Map<String, Object> jwks = webClient.get().uri(uri).retrieve().bodyToMono(Map.class).block();

    if (jwks == null || !jwks.containsKey("keys")) {
      throw new RuntimeException("JWKS 응답 형식 오류");
    }

    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
    Map<String, PublicKey> result = new ConcurrentHashMap<>();

    for (Map<String, Object> k : keys) {
      if (!"RSA".equals(k.get("kty"))) continue;

      String kid = (String) k.get("kid");
      String n = (String) k.get("n");
      String e = (String) k.get("e");

      if (kid == null || n == null || e == null) {
        log.warn("JWKS 키 정보 부족. skip. kid={}", kid);
        continue;
      }

      result.put(kid, createRsaPublicKey(n, e));
    }

    return result;
  }

  private PublicKey createRsaPublicKey(String n, String e) throws Exception {
    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
    return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
  }

  /** keyCache 업데이트 */
  private void applyNewKeys(Map<String, PublicKey> newKeys) {
    if (newKeys.isEmpty()) {
      log.warn("JWKS 로딩 성공했지만 key 없음. 기존 캐시 유지.");
      return;
    }

    keyCache.clear();
    keyCache.putAll(newKeys);
    lastJwksLoadedAt = System.currentTimeMillis();

    log.info("JWKS 업데이트 완료. key count={}", keyCache.size());
  }
}
