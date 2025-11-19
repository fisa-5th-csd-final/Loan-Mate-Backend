package com.fisa.bank.user.persistence.entity.id;

import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

import com.fisa.bank.common.persistence.id.BaseIdJavaType;

public class UserAuthIdJavaType extends BaseIdJavaType<Long, UserAuthId> {

  public static final UserAuthIdJavaType INSTANCE = new UserAuthIdJavaType();

  private static final JdbcType BIGINT_JDBC_TYPE = BigIntJdbcType.INSTANCE;

  public UserAuthIdJavaType() {
    super(UserAuthId.class, UserAuthId::of, LongJavaType.INSTANCE, BIGINT_JDBC_TYPE);
  }
}
