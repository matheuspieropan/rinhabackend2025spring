package org.pieropan.rinhaspring.http;

import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pagamentoProcessorFallback", url = "${pagamento.processor.fallback.url}")
public interface PagamentoProcessorFallbackClient {

    @PostMapping(value = "/payments", consumes = "application/json")
    Boolean processPayment(@RequestBody PagamentoProcessorRequestDto pagamentoProcessorRequestDto);
}