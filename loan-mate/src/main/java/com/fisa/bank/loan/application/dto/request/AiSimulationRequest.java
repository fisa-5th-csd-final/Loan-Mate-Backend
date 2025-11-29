package com.fisa.bank.loan.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class AiSimulationRequest {
  @Setter private Long userId;

  private final List<ChangeItem> changes;

  public AiSimulationRequest() {
    // changes가 null로 들어오면 NPE 방지
    this.changes = List.of();
  }
}
