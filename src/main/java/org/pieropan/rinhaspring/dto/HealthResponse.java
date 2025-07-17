package org.pieropan.rinhaspring.dto;

public record HealthResponse(boolean failing, int minResponseTime) {
}