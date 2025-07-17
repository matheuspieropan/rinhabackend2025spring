package org.pieropan.rinhaspring.service;

import org.bson.Document;
import org.pieropan.rinhaspring.dto.PagamentoProcessor;
import org.pieropan.rinhaspring.dto.PagamentoSummaryResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

@Service
public class PagamentoSummaryService {

    private final MongoTemplate mongoTemplate;

    public PagamentoSummaryService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public PagamentoSummaryResponse summary(String from, String to) {
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

        var results = mongoTemplate.aggregate(aggregation, "payments", Document.class).getMappedResults();

        BigDecimal defaultAmount = BigDecimal.ZERO;
        long defaultRequests = 0;
        BigDecimal fallbackAmount = BigDecimal.ZERO;
        long fallbackRequests = 0;

        for (Document doc : results) {
            Boolean isDefault = doc.getBoolean("_id");

            Number amountNumber = doc.get("totalAmount", Number.class);
            BigDecimal amount = amountNumber != null
                    ? new BigDecimal(amountNumber.toString())
                    : BigDecimal.ZERO;

            int count = doc.getInteger("totalRequests", 0);

            if (Boolean.TRUE.equals(isDefault)) {
                defaultAmount = amount;
                defaultRequests = count;
            } else {
                fallbackAmount = amount;
                fallbackRequests = count;
            }
        }

        PagamentoProcessor defaultSummary = new PagamentoProcessor((int) defaultRequests, defaultAmount);
        PagamentoProcessor fallbackSummary = new PagamentoProcessor((int) fallbackRequests, fallbackAmount);

        return new PagamentoSummaryResponse(defaultSummary, fallbackSummary);
    }

    public Instant parseFlexibleDate(String dateStr) {
        return OffsetDateTime.parse(dateStr).toInstant();
    }
}