package org.pieropan.rinhaspring.agendador;

import org.pieropan.rinhaspring.dto.HealthResponse;
import org.pieropan.rinhaspring.dto.MelhorOpcao;
import org.pieropan.rinhaspring.http.PagamentoProcessorDefaultClient;
import org.pieropan.rinhaspring.http.PagamentoProcessorFallbackClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
public class PagamentoHelthCheckJob {

    private final PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient;

    private final PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient;

    public static MelhorOpcao MELHOR_OPCAO = new MelhorOpcao(true, 5000);

    public PagamentoHelthCheckJob(PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient, PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient) {
        this.pagamentoProcessorDefaultClient = pagamentoProcessorDefaultClient;
        this.pagamentoProcessorFallbackClient = pagamentoProcessorFallbackClient;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 5000)
    public void checaProcessadorDefault() {
        HealthResponse healthResponseDefault = pagamentoProcessorDefaultClient.healthCheck();
        int RESPONSE_TIME_PADRAO = healthResponseDefault.failing() ? 10000 : healthResponseDefault.minResponseTime();

        HealthResponse healthResponseFallback = pagamentoProcessorFallbackClient.healthCheck();
        int RESPONSE_TIME_FALLBACK = healthResponseFallback.failing() ? 10000 : healthResponseFallback.minResponseTime();

        if (healthResponseDefault.failing() && healthResponseFallback.failing()) {
            MELHOR_OPCAO = null;
            return;
        }

        if (RESPONSE_TIME_PADRAO < RESPONSE_TIME_FALLBACK) {
            MELHOR_OPCAO = new MelhorOpcao(true, RESPONSE_TIME_PADRAO);
            return;
        } else if (RESPONSE_TIME_PADRAO == RESPONSE_TIME_FALLBACK) {
            MELHOR_OPCAO = new MelhorOpcao(true, RESPONSE_TIME_PADRAO);
            return;
        }

        MELHOR_OPCAO = new MelhorOpcao(false, RESPONSE_TIME_FALLBACK);
    }
}