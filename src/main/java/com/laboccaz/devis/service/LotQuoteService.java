package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.LotQuoteCreateDto;
import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
public class LotQuoteService {

    private final BubbleApiService bubbleApiService;
    private final ClientService clientService;

    public LotQuoteService(BubbleApiService bubbleApiService, ClientService clientService) {
        this.bubbleApiService = bubbleApiService;
        this.clientService = clientService;
    }

    /**
     * Crée un devis lot :
     * 1. Upsert du client dans Client_externe si nécessaire
     * 2. Création de la Commande dans Bubble avec tous les champs du lot
     */
    public String createLotQuote(LotQuoteCreateDto dto) {
        // Réutiliser l'upsert client existant via un DTO adapté
        QuoteRequestCreateDto clientDto = new QuoteRequestCreateDto();
        clientDto.setClientName(dto.getClientName());
        clientDto.setClientEmail(dto.getClientEmail());
        clientDto.setClientPhone(dto.getClientPhone());
        clientDto.setCompanyName(dto.getCompanyName());
        clientService.upsertFromQuoteDto(clientDto);

        // Créer la commande lot dans Bubble
        return bubbleApiService.createLotQuote(dto);
    }

    public String getLotQuotes() {
        return bubbleApiService.getCommandes();
    }

    public String getLotQuoteById(String id) {
        return bubbleApiService.getCommandeById(id);
    }

    public List<Map<String, Object>> getLotQuotesEnriched() {
        return bubbleApiService.getAllCommandesEnriched();
    }

    public Map<String, Object> getLotQuoteEnrichedById(String id) {
        return bubbleApiService.getCommandeEnriched(id);
    }

    public String deleteLotQuote(String id) {
        return bubbleApiService.deleteCommande(id);
    }

    public String updateLotQuoteStatus(String id, String status) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("admin_status", status);
        return bubbleApiService.patchCommandeFields(id, fields);
    }

    public String archiveLotQuote(String id, String archiveReason, String archiveReasonCustom) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("admin_status", "ARCHIVED");
        if (archiveReason != null)
            fields.put("archive_reason", archiveReason);
        if (archiveReasonCustom != null && !archiveReasonCustom.isBlank())
            fields.put("archive_reason_custom", archiveReasonCustom);
        return bubbleApiService.patchCommandeFields(id, fields);
    }

    public String rejectLotQuote(String id, String refusalReason, String refusalReasonCustom) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("admin_status", "REJECTED");
        if (refusalReason != null)
            fields.put("refusal_reason", refusalReason);
        if (refusalReasonCustom != null && !refusalReasonCustom.isBlank())
            fields.put("refusal_reason_custom", refusalReasonCustom);
        return bubbleApiService.patchCommandeFields(id, fields);
    }

}