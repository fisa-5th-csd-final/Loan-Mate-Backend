package com.fisa.bank.common.application.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fisa.bank.common.application.util.JsonNodeMapper;

@Component
@RequiredArgsConstructor
public class CoreBankingClient {

  private final OAuth2AuthorizedClientManager authorizedClientManager;
  private final WebClient.Builder builder;

  @Value("${AUTHORIZATION_URL}")
  private String BASE_URL;

  private WebClient getClient(String token) {
    return builder.defaultHeader("Authorization", "Bearer " + token).build();
  }

  private Authentication getAuth() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  /** OAuth2 AccessToken 만료 시 자동 갱신. */
  private String getAccessToken(Authentication authentication) {

    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      throw new IllegalStateException("OAuth2AuthenticationToken 이 존재하지 않습니다. 인증되지 않은 요청입니다.");
    }

    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      throw new IllegalStateException("Request attributes를 찾을 수 없습니다.");
    }

    OAuth2AuthorizeRequest authorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(
                oauthToken.getAuthorizedClientRegistrationId())
            .principal(oauthToken)
            .attribute("jakarta.servlet.http.HttpServletRequest", attrs.getRequest())
            .attribute("jakarta.servlet.http.HttpServletResponse", attrs.getResponse())
            .build();

    OAuth2AuthorizedClient client = authorizedClientManager.authorize(authorizeRequest);

    if (client == null) {
      throw new IllegalStateException("AuthorizedClient 를 찾을 수 없습니다.");
    }

    return client.getAccessToken().getTokenValue();
  }

  public <T> T fetchOne(String endpoint, Class<T> clazz) {
    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    JsonNode root =
        getClient(token)
            .get()
            .uri(BASE_URL + "/api/" + endpoint)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");
    return JsonNodeMapper.map(dataNode, clazz);
  }

  public <T> List<T> fetchList(String endpoint, Class<T> clazz) {
    Authentication auth = getAuth();
    String token = getAccessToken(auth);

    JsonNode root =
        getClient(token)
            .get()
            .uri(BASE_URL + "/api/" + endpoint)
            .retrieve()
            .bodyToMono(JsonNode.class)
            .block();

    if (root == null || root.isNull()) {
      throw new IllegalStateException("응답이 null 입니다.");
    }

    JsonNode dataNode = root.path("data");

    if (!dataNode.isArray()) {
      throw new IllegalStateException("data 노드가 배열이 아닙니다.");
    }

    return StreamSupport.stream(dataNode.spliterator(), false)
        .map(node -> JsonNodeMapper.map(node, clazz))
        .collect(Collectors.toList());
  }
}
