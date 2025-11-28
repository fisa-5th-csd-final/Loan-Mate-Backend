package com.fisa.bank.common.config;

import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

  private final ObjectMapper objectMapper;

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration cacheConfiguration = defaultCacheConfiguration();

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration)
        .transactionAware()
        .build();
  }

  // Redis 와 통신하는 통신 모듈 설정
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);

    var keySerializer = new StringRedisSerializer();
    var valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

    // 직렬화 설정
    redisTemplate.setKeySerializer(keySerializer);
    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashKeySerializer(keySerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);

    return redisTemplate;
  }

  // Redis 캐시 추상화 설정
  private RedisCacheConfiguration defaultCacheConfiguration() {
    var keySerializer = new StringRedisSerializer();
    var valueSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

    return RedisCacheConfiguration.defaultCacheConfig()
        .serializeKeysWith(SerializationPair.fromSerializer(keySerializer))
        .serializeValuesWith(SerializationPair.fromSerializer(valueSerializer))
        .entryTtl(Duration.ofDays(1))
        .disableCachingNullValues();
  }

  private ObjectMapper redisObjectMapper() {
    var typeValidator =
        BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();

    ObjectMapper mapper = objectMapper.copy();
    mapper.activateDefaultTyping(
        typeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
