package org.pieropan.rinhaspring.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PagamentoSummaryResponse(@JsonProperty("default") PagamentoProcessor defaultValue,
                                       PagamentoProcessor fallback) {
}