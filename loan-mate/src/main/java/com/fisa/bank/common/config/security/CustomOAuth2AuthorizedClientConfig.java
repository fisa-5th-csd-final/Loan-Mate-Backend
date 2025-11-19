package com.fisa.bank.common.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

public class CustomOAuth2AuthorizedClientConfig {
  @Bean
  public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(
      JdbcTemplate jdbcTemplate, ClientRegistrationRepository clientRegistrationRepository) {
    return new JdbcOAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository);
  }
}
