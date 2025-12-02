package com.fisa.bank.account.application.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record AiExpenditureResponse(JsonNode recommendation) {}
