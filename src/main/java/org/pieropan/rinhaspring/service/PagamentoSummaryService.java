package org.pieropan.rinhaspring.service;

import org.bson.Document;
import org.pieropan.rinhaspring.dto.PagamentoProcessor;
import org.pieropan.rinhaspring.dto.PagamentoSummaryResponse;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class PagamentoSummaryService {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    public PagamentoSummaryService(ReactiveMongoTemplate reactiveMongoTemplate) {
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    public Mono<PagamentoSummaryResponse> summary(String from, String to) {
        Instant fromDate = parseFlexibleDate(from);
        Instant toDate = parseFlexibleDate(to);

        Criteria dateCriteria = Criteria.where("createdAt")
                .gte(fromDate)
                .lte(toDate);

        Aggregation aggregation = newAggregation(
                match(dateCriteria),
                group("paymentProcessorDefault")
                        .sum("amount").as("totalAmount")
                        .count().as("totalRequests")
        );

        return reactiveMongoTemplate
                .aggregate(aggregation, "payments", Document.class)
                .collectList()
                .map(documents -> {
                    // Inicializa os valores
                    BigDecimal defaultAmount = BigDecimal.ZERO;
                    long defaultRequests = 0;
                    BigDecimal fallbackAmount = BigDecimal.ZERO;
                    long fallbackRequests = 0;

                    for (Document doc : documents) {
                        Boolean isDefault = doc.getBoolean("_id");
                        Number totalAmount = doc.get("totalAmount", Number.class);
                        int totalRequests = doc.getInteger("totalRequests", 0);

                        BigDecimal amount = totalAmount != null
                                ? new BigDecimal(totalAmount.toString())
                                : BigDecimal.ZERO;

                        if (Boolean.TRUE.equals(isDefault)) {
                            defaultAmount = amount;
                            defaultRequests = totalRequests;
                        } else {
                            fallbackAmount = amount;
                            fallbackRequests = totalRequests;
                        }
                    }

                    PagamentoProcessor defaultSummary = new PagamentoProcessor((int) defaultRequests, defaultAmount);
                    PagamentoProcessor fallbackSummary = new PagamentoProcessor((int) fallbackRequests, fallbackAmount);

                    return new PagamentoSummaryResponse(defaultSummary, fallbackSummary);
                });
    }

    public Instant parseFlexibleDate(String dateStr) {
        return OffsetDateTime.parse(dateStr).toInstant();
    }
}