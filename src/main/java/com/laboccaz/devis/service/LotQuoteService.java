package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.LotQuoteCreateDto;
import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.stereotype.Service;

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
}