package org.pieropan.rinhaspring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoProcessorRequestDto(String correlationId, BigDecimal amount,
                                           @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
                                           Instant requestedAt) {
}