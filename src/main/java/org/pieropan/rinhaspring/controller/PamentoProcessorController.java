package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    public PamentoProcessorController(PagamentoRepository pagamentoRepository, @Qualifier("defaultProcessorWebClient") WebClient defaultWebClient, @Qualifier("fallbackProcessorWebClient") WebClient fallbackWebClient) {
        this.pamentoProcessorService = new PamentoProcessorService(pagamentoRepository, defaultWebClient, fallbackWebClient);
    }

    @PostMapping
    public Mono<Void> pagar(@RequestBody PagamentoRequestDto pagamentoRequestDto) {
        //virtualThread.submit(() -> pamentoProcessorService.pagar(pagamentoRequestDto));
        return Mono.empty();
    }
}