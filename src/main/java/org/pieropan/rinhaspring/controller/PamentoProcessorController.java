package org.pieropan.rinhaspring.controller;

import feign.jackson.JacksonEncoder;
import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    private final ExecutorService virtualThread = Executors.newVirtualThreadPerTaskExecutor();

    public PamentoProcessorController(PagamentoRepository pagamentoRepository,
                                      @Value("${pagamento.processor.fallback.url}") String pagamentoProcessorFallbackUrl,
                                      @Value("${pagamento.processor.default.url}") String pagamentoProcessorDefaultUrl,
                                      JacksonEncoder jacksonEncoder) {
        this.pamentoProcessorService = new PamentoProcessorService(pagamentoRepository,
                pagamentoProcessorFallbackUrl,
                pagamentoProcessorDefaultUrl,
                jacksonEncoder);
    }

    @PostMapping
    public void pagar(@RequestBody PagamentoRequestDto pagamentoRequestDto) {
        virtualThread.submit(() -> pamentoProcessorService.pagar(pagamentoRequestDto));
    }
}