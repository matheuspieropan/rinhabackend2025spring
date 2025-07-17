package org.pieropan.rinhaspring.http;

import org.pieropan.rinhaspring.dto.HealthResponse;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pagamentoProcessorFallback", url = "${pagamento.processor.fallback.url}")
public interface PagamentoProcessorFallbackClient {

    @PostMapping(value = "/payments", consumes = "application/json")
    void processPayment(@RequestBody PagamentoProcessorRequestDto pagamentoProcessorRequestDto);

    @GetMapping(value = "/payments/service-health", consumes = "application/json")
    HealthResponse healthCheck();
}