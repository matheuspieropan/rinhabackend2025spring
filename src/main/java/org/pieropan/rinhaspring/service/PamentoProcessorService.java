package org.pieropan.rinhaspring.service;

import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.pieropan.rinhaspring.dto.MelhorOpcao;
import org.pieropan.rinhaspring.dto.PagamentoProcessorRequestDto;
import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.http.PagamentoProcessorDefaultClient;
import org.pieropan.rinhaspring.http.PagamentoProcessorFallbackClient;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.pieropan.rinhaspring.agendador.PagamentoHelthCheckJob.MELHOR_OPCAO;

@Service
public class PamentoProcessorService {

    public final String pagamentoProcessorDefaultUrl;

    public final String pagamentoProcessorFallbackUrl;

    public static Queue<PagamentoRequestDto> paymentsPending = new ConcurrentLinkedQueue<>();

    private final PagamentoRepository pagamentoRepository;

    private final JacksonEncoder jacksonEncoder;

    private static final Logger log = LoggerFactory.getLogger(PamentoProcessorService.class);

    public PamentoProcessorService(PagamentoRepository pagamentoRepository,
                                   @Value("${pagamento.processor.fallback.url}") String pagamentoProcessorFallbackUrl,
                                   @Value("${pagamento.processor.default.url}") String pagamentoProcessorDefaultUrl,
                                   JacksonEncoder jacksonEncoder) {

        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoProcessorFallbackUrl = pagamentoProcessorFallbackUrl;
        this.pagamentoProcessorDefaultUrl = pagamentoProcessorDefaultUrl;
        this.jacksonEncoder = jacksonEncoder;
    }

    public boolean pagarViaAgendador(PagamentoRequestDto pagamentoRequestDto) {
        Instant createdAt = Instant.now();
        boolean sucesso = enviarRequisicao(pagamentoRequestDto, createdAt);

        if (sucesso) {
            salvarDocument(pagamentoRequestDto, true, createdAt);
            paymentsPending.remove(pagamentoRequestDto);
            log.info("-- Conseguiu realizar pagamento --");
        }

        return sucesso;
    }

    public void pagar(PagamentoRequestDto pagamentoRequestDto) {
        try {

            Instant createdAt = Instant.now();
            boolean sucesso = enviarRequisicao(pagamentoRequestDto, createdAt);

            if (sucesso) {
                salvarDocument(pagamentoRequestDto, true, createdAt);
                return;
            }
            paymentsPending.add(pagamentoRequestDto);
        } catch (Exception e) {
            System.out.println("Erro processPayment(PaymentRequest paymentRequest)");
        }
    }

    public Boolean enviarRequisicao(PagamentoRequestDto pagamentoRequestDto, Instant createdAt) {

        PagamentoProcessorRequestDto pagamentoProcessorRequestDto = criarPagamentoProcessorRequest(pagamentoRequestDto, createdAt);
        MelhorOpcao melhorOpcao = MELHOR_OPCAO;

        if (melhorOpcao == null) {
            return false;
        }

        try {

            if (melhorOpcao.processadorDefault()) {
                clientDefaultHttp().processPayment(pagamentoProcessorRequestDto);
            } else {
                clientFallbackHttp().processPayment(pagamentoProcessorRequestDto);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static PagamentoProcessorRequestDto criarPagamentoProcessorRequest(PagamentoRequestDto pagamentoRequestDto, Instant createdAt) {
        return new PagamentoProcessorRequestDto(pagamentoRequestDto.correlationId(), pagamentoRequestDto.amount(), createdAt);
    }

    public void salvarDocument(PagamentoRequestDto pagamentoRequestDto, boolean isDefault, Instant createdAt) {
        PagamentoDocument doc = new PagamentoDocument();

        doc.setCorrelationId(pagamentoRequestDto.correlationId());
        doc.setAmount(pagamentoRequestDto.amount());
        doc.setPaymentProcessorDefault(isDefault);
        doc.setCreatedAt(createdAt);

        pagamentoRepository.save(doc);
    }

    public synchronized PagamentoProcessorDefaultClient clientDefaultHttp() {
        int timeout = MELHOR_OPCAO.timeoutIndicado();
        return Feign.builder().
                encoder(jacksonEncoder).
                decoder(new JacksonDecoder()).
                options(new Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, false)).
                target(PagamentoProcessorDefaultClient.class, pagamentoProcessorDefaultUrl);
    }

    public synchronized PagamentoProcessorFallbackClient clientFallbackHttp() {
        int timeout = MELHOR_OPCAO.timeoutIndicado() + 500;
        return Feign.builder().
                encoder(new JacksonEncoder()).
                decoder(new JacksonDecoder()).
                options(new Request.Options(timeout, TimeUnit.SECONDS, timeout, TimeUnit.SECONDS, false)).
                target(PagamentoProcessorFallbackClient.class, pagamentoProcessorFallbackUrl);
    }
}