package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PamentoProcessorService {

    public static Queue<PagamentoRequestDto> paymentsPending = new ConcurrentLinkedQueue<>();

    private final PagamentoRepository pagamentoRepository;
    private final WebClient defaultWebClient;
    private final WebClient fallbackWebClient;

    public PamentoProcessorService(PagamentoRepository pagamentoRepository,
                                   @Qualifier("defaultProcessorWebClient") WebClient defaultWebClient,
                                   @Qualifier("fallbackProcessorWebClient") WebClient fallbackWebClient) {
        this.pagamentoRepository = pagamentoRepository;
        this.defaultWebClient = defaultWebClient;
        this.fallbackWebClient = fallbackWebClient;
    }

    public Mono<Void> pagarViaAgendador(PagamentoRequestDto pagamentoRequestDto) {
        Instant createdAt = Instant.now();

        return enviarRequisicao(pagamentoRequestDto, true, createdAt)
                .flatMap(sucesso -> {
                    if (sucesso) {
                        return salvarDocument(pagamentoRequestDto, true, createdAt)
                                .doOnSuccess(v -> paymentsPending.remove(pagamentoRequestDto));
                    }
                    return Mono.empty();
                });
    }

    public Mono<Void> pagar(PagamentoRequestDto pagamentoRequestDto) {
        Instant createdAt = Instant.now();

        return enviarRequisicao(pagamentoRequestDto, true, createdAt)
                .flatMap(sucesso -> {
                    if (sucesso) {
                        return salvarDocument(pagamentoRequestDto, true, createdAt);
                    } else {
                        return enviarRequisicao(pagamentoRequestDto, false, createdAt)
                                .flatMap(sucessoFallback -> {
                                    if (sucessoFallback) {
                                        return salvarDocument(pagamentoRequestDto, false, createdAt);
                                    } else {
                                        paymentsPending.add(pagamentoRequestDto);
                                        return Mono.empty();
                                    }
                                });
                    }
                })
                .onErrorResume(ex -> {
                    System.out.println("Erro processPayment: " + ex.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Boolean> enviarRequisicao(PagamentoRequestDto pagamentoRequestDto, boolean isDefault, Instant createdAt) {
        PagamentoProcessorRequestDto body = criarPagamentoProcessorRequest(pagamentoRequestDto, createdAt);

        WebClient client = isDefault ? defaultWebClient : fallbackWebClient;

        return client.post()
                .uri("/payments")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorResume(ex -> Mono.just(false));
    }

    private Mono<Void> salvarDocument(PagamentoRequestDto pagamentoRequestDto, boolean isDefault, Instant createdAt) {
        PagamentoDocument doc = new PagamentoDocument();
        doc.setCorrelationId(pagamentoRequestDto.correlationId());
        doc.setAmount(pagamentoRequestDto.amount());
        doc.setPaymentProcessorDefault(isDefault);
        doc.setCreatedAt(createdAt);

        return pagamentoRepository.save(doc).then();
    }

    public static PagamentoProcessorRequestDto criarPagamentoProcessorRequest(PagamentoRequestDto dto, Instant createdAt) {
        return new PagamentoProcessorRequestDto(dto.correlationId(), dto.amount(), createdAt);
    }
}