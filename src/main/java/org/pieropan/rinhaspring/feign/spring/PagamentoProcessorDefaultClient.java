package org.pieropan.rinhaspring.feign.spring;

import org.pieropan.rinhaspring.dto.HealthResponse;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "pagamentoProcessorDefault", url = "${pagamento.processor.default.url}")
public interface PagamentoProcessorDefaultClient {

    @PostMapping(value = "/payments", consumes = "application/json")
    void processPayment(PagamentoProcessorRequestDto pagamentoProcessorRequestDto);

    @GetMapping(value = "/payments/service-health", consumes = "application/json")
    HealthResponse healthCheck();
}