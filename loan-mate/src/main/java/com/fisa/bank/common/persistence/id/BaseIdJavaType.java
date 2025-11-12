package com.fisa.bank.common.persistence.id;

import java.util.function.Function;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

/**
 * Hibernate Custom Type의 공통 구현체 T: ID의 기본 타입 (예: Long, UUID) ID: BaseId<T>를 상속받는 구체적인 ID 클래스 (예:
 * UserId)
 */
public abstract class BaseIdJavaType<T, ID extends BaseId<T>> extends AbstractClassJavaType<ID> {

  private final Function<T, ID> factory;
  private final JavaType<T> baseJavaType;
  private final JdbcType recommendedJdbcType;

  protected BaseIdJavaType(
      Class<ID> idClass,
      Function<T, ID> factory,
      JavaType<T> baseJavaType,
      JdbcType recommendedJdbcType) {
    super(idClass);
    this.factory = factory;
    this.baseJavaType = baseJavaType;
    this.recommendedJdbcType = recommendedJdbcType;
  }

  @Override
  public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
    return recommendedJdbcType;
  }

  @Override
  public ID fromString(CharSequence s) {
    T baseValue = baseJavaType.fromString(s);
    return factory.apply(baseValue);
  }

  @SuppressWarnings("unchecked") // 경고 억제
  @Override
  public <X> X unwrap(ID value, Class<X> type, WrapperOptions options) {
    if (value == null) return null;
    T rawValue = value.getValue();

    return baseJavaType.unwrap(rawValue, type, options);
  }

  @Override
  public <X> ID wrap(X value, WrapperOptions options) {
    if (value instanceof BaseId<?> baseId) {
      return (ID) baseId;
    }

    T baseValue = baseJavaType.wrap(value, options);
    return factory.apply(baseValue);
  }
}
