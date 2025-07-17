package org.pieropan.rinhaspring.http;

import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pagamentoProcessorDefault", url = "${pagamento.processor.default.url}")
public interface PagamentoProcessorDefaultClient {

    @PostMapping(value = "/payments", consumes = "application/json")
    void processPayment(@RequestBody PagamentoProcessorRequestDto pagamentoProcessorRequestDto);
}