package com.fisa.bank.common.persistence.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 필터 정의
// @FilterDef(
//        name = "deletedFilter", // 정의할 필터 이름
//        parameters = @ParamDef(name = "isDeleted", type = Boolean.class) // 필터에 사용될 파라미터
// )
//// 실제 적용되는 핕터
// @Filter(
//        name = "deletedFilter", // 적용할 필터 이름
//        condition = "is_deleted = :isDeleted" // 필터 조건 - sql 실행 시 해당 조건에 따라 실행
// )
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

  // TODO: Auditing Listener 적용

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  public BaseEntity() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    this.deletedAt = null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
