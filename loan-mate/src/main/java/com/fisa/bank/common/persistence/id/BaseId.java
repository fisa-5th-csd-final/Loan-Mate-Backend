package com.fisa.bank.common.persistence.id;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public abstract class BaseId<T> {

  private final T value;

  // 자식 클래스에서 lombok을 쓰기 위해 부모 클래스에 기본 생성자를 필요로 함
  protected BaseId() {
    this.value = null;
  }

  // 외부에서 인스턴스화 불가하게 처리
  protected BaseId(T value) {
    if (value == null) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() + "null 값 예외 발생");
    }
    commonValidate(value);
    specificValidate(value);
    this.value = value;
  }

  // 공통 유효성 처리 로직
  private void commonValidate(T value) {
    if (value instanceof Long l) {
      if (l < 0) {
        throw new IllegalArgumentException("Id가 0보다 작음: " + this.getClass().getSimpleName());
      }
    }
  }

  protected void specificValidate(T value) {
    // 추가로 자식 클래스에서 구현할 검증 로직
  }
}
