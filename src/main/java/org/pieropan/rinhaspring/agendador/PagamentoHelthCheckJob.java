package org.pieropan.rinhaspring.agendador;

import org.pieropan.rinhaspring.dto.HealthResponse;
import org.pieropan.rinhaspring.dto.MelhorOpcao;
import org.pieropan.rinhaspring.feign.spring.PagamentoProcessorDefaultClient;
import org.pieropan.rinhaspring.feign.spring.PagamentoProcessorFallbackClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PagamentoHelthCheckJob {

    private final PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient;

    private final PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient;

    public static MelhorOpcao MELHOR_OPCAO = new MelhorOpcao(true, 5000);

    public PagamentoHelthCheckJob(PagamentoProcessorDefaultClient pagamentoProcessorDefaultClient, PagamentoProcessorFallbackClient pagamentoProcessorFallbackClient) {
        this.pagamentoProcessorDefaultClient = pagamentoProcessorDefaultClient;
        this.pagamentoProcessorFallbackClient = pagamentoProcessorFallbackClient;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5500)
    public void checaProcessadorDefault() {

        HealthResponse healthResponseDefault = null;
        HealthResponse healthResponseFallback = null;

        try {
            healthResponseDefault = pagamentoProcessorDefaultClient.healthCheck();
        } catch (Exception ignored) {
        }

        try {
            healthResponseFallback = pagamentoProcessorFallbackClient.healthCheck();
        } catch (Exception ignored) {
        }

        if (healthResponseDefault == null && healthResponseFallback == null) {
            MELHOR_OPCAO = null;
            return;
        }

        boolean ambosResponderam = healthResponseDefault != null && healthResponseFallback != null;
        if (ambosResponderam) {

            boolean defaultDisponivel = !healthResponseDefault.failing();
            boolean fallbackDisponivel = !healthResponseFallback.failing();

            if (defaultDisponivel && fallbackDisponivel) {

                MELHOR_OPCAO = new MelhorOpcao(true, healthResponseDefault.minResponseTime());

            } else if (defaultDisponivel) {

                MELHOR_OPCAO = new MelhorOpcao(true, healthResponseDefault.minResponseTime());

            } else if (fallbackDisponivel) {

                MELHOR_OPCAO = new MelhorOpcao(false, healthResponseFallback.minResponseTime());
            } else {

                MELHOR_OPCAO = null;
            }

        } else if (healthResponseDefault != null && !healthResponseDefault.failing()) {

            MELHOR_OPCAO = new MelhorOpcao(true, healthResponseDefault.minResponseTime());

        } else if (healthResponseFallback != null && !healthResponseFallback.failing()) {
            MELHOR_OPCAO = new MelhorOpcao(false, healthResponseFallback.minResponseTime());

        } else {

            MELHOR_OPCAO = null;
        }
    }
}