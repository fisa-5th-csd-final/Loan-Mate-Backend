package com.fisa.bank.common.config.security;

import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  @Order(1)
  public SecurityFilterChain oauth2SecurityFilterChain(
      HttpSecurity http,
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository,
      OAuth2AuthorizedClientService authorizedClientService,
      CustomOAuth2SuccessHandler customOAuth2SuccessHandler)
      throws Exception {
    var repo = new HttpSessionOAuth2AuthorizationRequestRepository();

    var resolver =
        new DefaultOAuth2AuthorizationRequestResolver(
            clientRegistrationRepository, "/oauth2/authorization" // 기본 엔드포인트
            );
    resolver.setAuthorizationRequestCustomizer(
        OAuth2AuthorizationRequestCustomizers.withPkce() // code challenge 추가
        );

    http.securityMatcher("/oauth2/**", "/login/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login/**", "/oauth2/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .oauth2Login(
            oauth ->
                oauth
                    .authorizedClientRepository(authorizedClientRepository)
                    .authorizedClientService(authorizedClientService)

                    // /userinfo 호출 안함
                    .userInfoEndpoint(
                        userInfo ->
                            userInfo.userService(
                                userRequest -> {
                                  Map<String, Object> attributes = Map.of("sub", "user");
                                  return new DefaultOAuth2User(
                                      Collections.emptyList(), attributes, "sub");
                                }))
                    .authorizationEndpoint(
                        ep ->
                            ep.authorizationRequestRepository(repo) // 세션 저장
                                .authorizationRequestResolver(resolver))
                    .successHandler(customOAuth2SuccessHandler));
    return http.build();
  }
}
