package org.pieropan.rinhaspring.dto;

import java.math.BigDecimal;

public record PagamentoProcessor(int totalRequests, BigDecimal totalAmount) {
}