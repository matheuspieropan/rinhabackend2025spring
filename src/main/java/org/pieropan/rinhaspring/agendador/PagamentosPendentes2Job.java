//package org.pieropan.rinhaspring.agendador;
//
//import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
//import org.pieropan.rinhaspring.service.PamentoProcessorService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import static org.pieropan.rinhaspring.service.PamentoProcessorService.paymentsPending;
//
//@Component
//public class PagamentosPendentes2Job {
//
//    private static final Logger log = LoggerFactory.getLogger(PagamentosPendentes2Job.class);
//
//    private final PamentoProcessorService pamentoProcessorService;
//
//    public PagamentosPendentes2Job(PamentoProcessorService pamentoProcessorService) {
//        this.pamentoProcessorService = pamentoProcessorService;
//    }
//
//    @Scheduled(initialDelay = 10000, fixedDelay = 500)
//    public void reprocessa() {
//        if (paymentsPending.isEmpty()) {
//            return;
//        }
//
//        log.info("Iniciando reprocessamento. Total pendentes: {}", paymentsPending.size());
//
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//
//        for (int i = 0; i < 2; i++) {
//            executor.submit(this::pagarPorAgendador);
//        }
//
//        executor.shutdown();
//    }
//
//    private void pagarPorAgendador() {
//        while (!paymentsPending.isEmpty()) {
//            try {
//                PagamentoRequestDto pagamentoRequestDto = paymentsPending.take();
//                boolean falhou = !pamentoProcessorService.pagarPorAgendador(pagamentoRequestDto);
//
//                if (falhou) {
//                    paymentsPending.put(pagamentoRequestDto);
//                }
//
//            } catch (Exception ignored) {
//            }
//        }
//    }
//}