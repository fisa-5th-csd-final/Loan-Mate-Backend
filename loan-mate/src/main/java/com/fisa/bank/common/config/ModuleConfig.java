package com.fisa.bank.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Java8 이후 도입된 java.time 패키지 (JSR 310) JavaTimeModule - Java8에 도입된 JSR 310 날짜/시간 모듈을 Jackson 라이브러리가
 * 처리할 수 있도록 해주는 모듈 이 모듈을 빈으로 등록해야 Jackson이 LocalDateTime, Clock.. 과 같은 타입을 역직렬화해준다.
 */
@Configuration
public class ModuleConfig {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
}
