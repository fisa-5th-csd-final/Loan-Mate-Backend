package com.fisa.bank.user.persistence.entity.id;

import com.fisa.bank.common.persistence.id.BaseId;

public class UserId extends BaseId<Long> {

  protected UserId() {
    super();
  }

  private UserId(Long value) {
    super(value);
  }

  public static UserId of(Long value) {
    return new UserId(value);
  }
}
