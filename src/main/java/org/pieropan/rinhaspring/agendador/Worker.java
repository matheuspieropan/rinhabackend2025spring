//package org.pieropan.rinhaspring.agendador;
//
//import jakarta.annotation.PostConstruct;
//import org.pieropan.rinhaspring.dto.PagamentoRequestDto;
//import org.pieropan.rinhaspring.service.PamentoProcessorService;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.LinkedBlockingQueue;
//
//@Component
//public class Worker {
//
//    private final PamentoProcessorService pamentoProcessorService;
//
//    public Worker(PamentoProcessorService pamentoProcessorService) {
//        this.pamentoProcessorService = pamentoProcessorService;
//    }
//
//    public static final BlockingQueue<PagamentoRequestDto> paymentsQueue = new LinkedBlockingQueue<>();
//
//    @PostConstruct
//    public void iniciarWorkers() {
//        int paralelismo = 4;
//        try (ExecutorService executor = Executors.newFixedThreadPool(paralelismo)) {
//
//            for (int i = 0; i < paralelismo; i++) {
//                executor.submit(this::extracted);
//            }
//        }
//    }
//
//    private void extracted() {
//        while (true) {
//            try {
//                PagamentoRequestDto pagamentoRequestDto = paymentsQueue.take();
//                boolean falhou = !pamentoProcessorService.pagarPorAgendador(pagamentoRequestDto);
//
//                if (falhou) {
//                    paymentsQueue.offer(pagamentoRequestDto);
//                }
//
//            } catch (Exception ignored) {
//            }
//        }
//    }
//}