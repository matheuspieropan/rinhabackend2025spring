package org.pieropan.rinhaspring.agendador;

import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.pieropan.rinhaspring.service.PamentoProcessorService.paymentsPending;

@Component
public class PagamentosPendentesJob {

    private static final Logger log = LoggerFactory.getLogger(PagamentosPendentesJob.class);

    private final PamentoProcessorService pamentoProcessorService;

    public PagamentosPendentesJob(PamentoProcessorService pamentoProcessorService) {
        this.pamentoProcessorService = pamentoProcessorService;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 500)
    public void reprocessa() {
        if (paymentsPending.isEmpty()) {
            return;
        }

        log.info("Iniciando reprocessamento. Total pendentes: {}", paymentsPending.size());

        for (PagamentoRequestDto pagamentoRequestDto : paymentsPending) {
            boolean falhou = !pamentoProcessorService.pagarViaAgendador(pagamentoRequestDto);
            if (falhou) {
                break;
            }
        }
    }
}