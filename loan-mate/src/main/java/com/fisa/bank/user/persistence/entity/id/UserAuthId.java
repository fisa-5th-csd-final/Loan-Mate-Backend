package com.fisa.bank.user.persistence.entity.id;

import com.fisa.bank.common.persistence.id.BaseId;

public class UserAuthId extends BaseId<Long> {

  protected UserAuthId() {
    super();
  }

  private UserAuthId(Long value) {
    super(value);
  }

  public static UserAuthId of(Long value) {
    return new UserAuthId(value);
  }
}
