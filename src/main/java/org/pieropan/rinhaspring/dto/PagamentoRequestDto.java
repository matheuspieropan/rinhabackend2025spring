package org.pieropan.rinhaspring.dto;

import java.math.BigDecimal;

public record PagamentoRequestDto(String correlationId, BigDecimal amount) {
}