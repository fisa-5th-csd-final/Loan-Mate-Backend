package com.fisa.bank.user.persistence.entity.id;

import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import com.fisa.bank.common.persistence.id.BaseIdJavaType;

public class UserIdJavaType extends BaseIdJavaType<Long, UserId> {

  public static final UserIdJavaType INSTANCE = new UserIdJavaType();

  private static final JdbcType BIGINT_JDBC_TYPE = BigIntJdbcType.INSTANCE;

  public UserIdJavaType() {
    super(
        UserId.class,
        UserId::of,
        org.hibernate.type.descriptor.java.LongJavaType.INSTANCE,
        BIGINT_JDBC_TYPE);
  }
}
