package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.http.PagamentoProcessorDefaultClient;
import org.pieropan.rinhaspring.http.PagamentoProcessorFallbackClient;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
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
                                      PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient,
                                      PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient) {
        this.pamentoProcessorService = new PamentoProcessorService(
                pagamentoRepository,
                pagamentoProcessorDefaultClient,
                pagamentoProcessorFallbackClient);
    }

    @PostMapping
    public void pagar(@RequestBody PagamentoRequestDto pagamentoRequestDto) {
        virtualThread.submit(() -> pamentoProcessorService.pagar(pagamentoRequestDto));
    }
}