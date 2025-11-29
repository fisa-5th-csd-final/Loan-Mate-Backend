package com.fisa.bank.loan.application.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
public class AiSimulationRequest {
  @Setter private Long userId;

  private final List<ChangeItem> changes;

  public AiSimulationRequest(@JsonProperty("changes") List<ChangeItem> changes) {
    this.changes = changes == null ? List.of() : changes;
  }
}
