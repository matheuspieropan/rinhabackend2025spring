package org.pieropan.rinhaspring.agendador;

import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.pieropan.rinhaspring.service.PamentoProcessorService.paymentsPending;

@Component
public class PagamentosPendentesJob {

    private final PamentoProcessorService pamentoProcessorService;

    public PagamentosPendentesJob(PamentoProcessorService pamentoProcessorService) {
        this.pamentoProcessorService = pamentoProcessorService;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 100)
    public void reprocessa() {
        if (paymentsPending.isEmpty()) {
            return;
        }

        for (PagamentoRequestDto pagamentoRequestDto : paymentsPending) {
            boolean falhou = !pamentoProcessorService.pagarPorAgendador(pagamentoRequestDto);
            if (falhou) {
                break;
            }
        }
    }
}