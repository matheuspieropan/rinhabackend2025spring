package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.http.PagamentoProcessorDefaultClient;
import org.pieropan.rinhaspring.http.PagamentoProcessorFallbackClient;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PamentoProcessorService {

    public static Queue<PagamentoRequestDto> paymentsPending = new ConcurrentLinkedQueue<>();

    private final PagamentoRepository pagamentoRepository;

    private final PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient;

    private final PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient;

    private static final Logger log = LoggerFactory.getLogger(PamentoProcessorService.class);

    public PamentoProcessorService(PagamentoRepository pagamentoRepository,
                                   PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient,
                                   PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient) {
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoProcessorDefaultClient = pagamentoProcessorDefaultClient;
        this.pagamentoProcessorFallbackClient = pagamentoProcessorFallbackClient;
    }

    public boolean pagarViaAgendador(PagamentoRequestDto pagamentoRequestDto) {
        Instant createdAt = Instant.now();
        boolean sucesso = enviarRequisicao(pagamentoRequestDto, true, createdAt);

        if (sucesso) {
            salvarDocument(pagamentoRequestDto, true, createdAt);
            paymentsPending.remove(pagamentoRequestDto);
        } else {
            sucesso = enviarRequisicao(pagamentoRequestDto, false, createdAt);
            if (sucesso) {
                salvarDocument(pagamentoRequestDto, false, createdAt);
                paymentsPending.remove(pagamentoRequestDto);
            }
        }
        log.info("-- Conseguiu realizar pagamento --");
        return sucesso;
    }

    public void pagar(PagamentoRequestDto pagamentoRequestDto) {
        try {

            Instant createdAt = Instant.now();
            boolean sucesso = enviarRequisicao(pagamentoRequestDto, true, createdAt);

            if (sucesso) {
                salvarDocument(pagamentoRequestDto, true, createdAt);
            } else {

                sucesso = enviarRequisicao(pagamentoRequestDto, false, createdAt);
                if (sucesso) {
                    salvarDocument(pagamentoRequestDto, false, createdAt);
                    return;
                }
                paymentsPending.add(pagamentoRequestDto);
            }
        } catch (Exception e) {
            System.out.println("Erro processPayment(PaymentRequest paymentRequest)");
        }
    }

    public Boolean enviarRequisicao(PagamentoRequestDto pagamentoRequestDto, boolean paymentProcessorDefault, Instant createdAt) {

        PagamentoProcessorRequestDto pagamentoProcessorRequestDto = criarPagamentoProcessorRequest(pagamentoRequestDto, createdAt);

        try {

            if (paymentProcessorDefault) {
                pagamentoProcessorDefaultClient.processPayment(pagamentoProcessorRequestDto);
                return true;
            }

            pagamentoProcessorFallbackClient.processPayment(pagamentoProcessorRequestDto);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static PagamentoProcessorRequestDto criarPagamentoProcessorRequest(PagamentoRequestDto pagamentoRequestDto, Instant createdAt) {
        return new PagamentoProcessorRequestDto(
                pagamentoRequestDto.correlationId(),
                pagamentoRequestDto.amount(),
                createdAt);
    }

    public void salvarDocument(PagamentoRequestDto pagamentoRequestDto, boolean isDefault, Instant createdAt) {
        PagamentoDocument doc = new PagamentoDocument();

        doc.setCorrelationId(pagamentoRequestDto.correlationId());
        doc.setAmount(pagamentoRequestDto.amount());
        doc.setPaymentProcessorDefault(isDefault);
        doc.setCreatedAt(createdAt);

        pagamentoRepository.save(doc);
    }
}