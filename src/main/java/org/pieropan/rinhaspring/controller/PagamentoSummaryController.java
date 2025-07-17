package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.dto.PagamentoProcessor;
import org.pieropan.rinhaspring.dto.PagamentoSummaryResponse;
import org.pieropan.rinhaspring.service.PagamentoSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments-summary")
public class PagamentoSummaryController {

    private final PagamentoSummaryService pagamentoSummaryService;

    public PagamentoSummaryController(PagamentoSummaryService pagamentoSummaryService) {
        this.pagamentoSummaryService = pagamentoSummaryService;
    }

    @GetMapping
    public Mono<PagamentoSummaryResponse> summary(@RequestParam("from") String from,
                                                  @RequestParam("to") String to) {
        return Mono.just(new PagamentoSummaryResponse(new PagamentoProcessor(
                0, BigDecimal.ONE), new PagamentoProcessor(0, BigDecimal.ZERO)));
        //return pagamentoSummaryService.summary(from, to);
    }
}