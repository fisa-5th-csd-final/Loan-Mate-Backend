package com.fisa.bank.user.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fisa.bank.user.persistence.entity.id.UserAuthId;
import com.fisa.bank.user.persistence.entity.id.UserAuthIdJavaType;

/** UserAuth 영속성 엔티티 (JPA Entity) 코어뱅킹 시스템의 사용자 인증 정보를 저장 */
@Entity
@Table(name = "user_auth_service")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserAuthEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JavaType(UserAuthIdJavaType.class)
  @JdbcTypeCode(SqlTypes.BIGINT)
  private UserAuthId id;

  /** 외부 시스템(CB)의 ID */
  @Column(nullable = false, unique = true)
  private Long coreBankingUserId;

  /** 내부 UserEntity PK */
  @Column(nullable = false, unique = true)
  private Long serviceUserId;
}
