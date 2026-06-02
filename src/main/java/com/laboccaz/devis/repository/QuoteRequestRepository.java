package com.laboccaz.devis.repository;

import com.laboccaz.devis.entity.QuoteRequest;
import com.laboccaz.devis.entity.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRequestRepository extends JpaRepository<QuoteRequest, Long> {

    List<QuoteRequest> findByStatus(QuoteStatus status);

    List<QuoteRequest> findByClientEmail(String clientEmail);
}