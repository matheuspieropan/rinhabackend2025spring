package org.pieropan.rinhaspring.http;

import feign.Headers;
import feign.RequestLine;
import org.pieropan.rinhaspring.dto.HealthResponse;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pagamentoProcessorDefault", url = "${pagamento.processor.default.url}")
public interface PagamentoProcessorDefaultClient {

    @RequestLine("POST /payments")
    @Headers("Content-Type: application/json")
    void processPayment(PagamentoProcessorRequestDto pagamentoProcessorRequestDto);

    @RequestLine("GET /payments/service-health")
    @Headers("Accept: application/json")
    HealthResponse healthCheck();
}