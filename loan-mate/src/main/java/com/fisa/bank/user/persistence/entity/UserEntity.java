package com.fisa.bank.user.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fisa.bank.common.persistence.entity.BaseEntity;
import com.fisa.bank.user.application.model.CreditRating;
import com.fisa.bank.user.application.model.CustomerLevel;
import com.fisa.bank.user.persistence.entity.id.UserId;
import com.fisa.bank.user.persistence.entity.id.UserIdJavaType;

/** User 영속성 엔티티 (JPA Entity) DB 테이블과 매핑되는 클래스 */
@Entity
@Table(name = "user_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class UserEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JavaType(UserIdJavaType.class)
  @JdbcTypeCode(SqlTypes.BIGINT)
  private UserId userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String address;

  /*
  @Column(nullable = false)
  private LocalDate birthday; */

  @Column(nullable = false)
  private String job;

  /*
  @Column(nullable = false)
  private BigDecimal income; */

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CreditRating creditLevel;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private CustomerLevel customerLevel;
}
