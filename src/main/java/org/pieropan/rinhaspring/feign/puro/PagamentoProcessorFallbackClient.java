package org.pieropan.rinhaspring.feign.puro;

import feign.Headers;
import feign.RequestLine;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;

public interface PagamentoProcessorFallbackClient {

    @RequestLine("POST /payments")
    @Headers("Content-Type: application/json")
    void processPayment(PagamentoProcessorRequestDto pagamentoProcessorRequestDto);
}