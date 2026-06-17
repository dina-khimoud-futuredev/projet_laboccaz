package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.stereotype.Service;




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
}