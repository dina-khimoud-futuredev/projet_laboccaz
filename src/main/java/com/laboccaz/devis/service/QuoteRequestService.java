package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.ArchiveQuoteDto;
import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import com.laboccaz.devis.entity.ArchiveReason;

import java.util.HashMap;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.laboccaz.devis.dto.RejectQuoteDto;
import com.laboccaz.devis.entity.RefusalReason;

@Service
public class QuoteRequestService {

    private final BubbleApiService bubbleApiService;
    private final ClientService clientService;

    public QuoteRequestService(BubbleApiService bubbleApiService, ClientService clientService) {
        this.bubbleApiService = bubbleApiService;
        this.clientService = clientService;
    }

    public String createQuoteRequest(QuoteRequestCreateDto dto) {
        clientService.upsertFromQuoteDto(dto);
        return bubbleApiService.createQuoteRequest(dto);
    }

    public String getAllQuoteRequests() {
        return bubbleApiService.getQuoteRequests();
    }

    public String getQuoteRequestById(String id) {
        return bubbleApiService.getQuoteRequestById(id);
    }

    public String updateStatus(String id, String status) {
        return bubbleApiService.updateQuoteRequestStatus(id, status);
    }

    public String deleteQuoteRequest(String id) {
        return bubbleApiService.deleteQuoteRequest(id);
    }

    public String updateQuoteRequest(String id, QuoteRequestCreateDto dto) {
        clientService.upsertFromQuoteDto(dto);
        return bubbleApiService.updateQuoteRequest(id, dto);
    }

    /**
     * Archive un devis :
     * - passe le statut à ARCHIVED dans Bubble
     * - enregistre le motif prédéfini et/ou le motif libre
     * - si archiveReason == AUTRE, archiveReasonCustom est obligatoire
     */
    public String archiveQuote(String id, ArchiveQuoteDto dto) {
        // Validation : motif libre obligatoire si AUTRE
        if (dto.getArchiveReason() == ArchiveReason.AUTRE
                && (dto.getArchiveReasonCustom() == null || dto.getArchiveReasonCustom().isBlank())) {
            throw new IllegalArgumentException("Un motif libre est obligatoire quand le motif est AUTRE.");
        }

        // Construire les champs à envoyer à Bubble
        Map<String, Object> extraFields = new HashMap<>();
        extraFields.put("status", "ARCHIVED");

        if (dto.getArchiveReason() != null) {
            extraFields.put("archive_reason", dto.getArchiveReason().name());
            extraFields.put("archive_reason_label", dto.getArchiveReason().getLabel());
        }

        if (dto.getArchiveReasonCustom() != null && !dto.getArchiveReasonCustom().isBlank()) {
            extraFields.put("archive_reason_custom", dto.getArchiveReasonCustom());
        }

        return bubbleApiService.patchQuoteFields(id, extraFields);
    }

    /**
     * Refuse une demande :
     * - passe le statut à REJECTED dans Bubble
     * - enregistre le motif prédéfini et/ou le motif libre
     * - si refusalReason == AUTRE, refusalReasonCustom est obligatoire
     */
    public String rejectQuote(String id, RejectQuoteDto dto) {
        if (dto.getRefusalReason() == RefusalReason.AUTRE
                && (dto.getRefusalReasonCustom() == null || dto.getRefusalReasonCustom().isBlank())) {
            throw new IllegalArgumentException("Un motif libre est obligatoire quand le motif est AUTRE.");
        }

        Map<String, Object> extraFields = new HashMap<>();
        extraFields.put("status", "REJECTED");

        if (dto.getRefusalReason() != null) {
            extraFields.put("refusal_reason", dto.getRefusalReason().name());
            extraFields.put("refusal_reason_label", dto.getRefusalReason().getLabel());
        }

        if (dto.getRefusalReasonCustom() != null && !dto.getRefusalReasonCustom().isBlank()) {
            extraFields.put("refusal_reason_custom", dto.getRefusalReasonCustom());
        }

        return bubbleApiService.patchQuoteFields(id, extraFields);
    }
}