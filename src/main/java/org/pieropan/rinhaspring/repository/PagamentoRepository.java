package org.pieropan.rinhaspring.repository;

import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PagamentoRepository extends ReactiveMongoRepository<PagamentoDocument, String> {
}