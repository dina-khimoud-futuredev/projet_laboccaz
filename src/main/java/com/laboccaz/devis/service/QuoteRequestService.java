package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.stereotype.Service;

@Service
public class QuoteRequestService {

    private final BubbleApiService bubbleApiService;

    public QuoteRequestService(BubbleApiService bubbleApiService) {
        this.bubbleApiService = bubbleApiService;
    }

    public String createQuoteRequest(QuoteRequestCreateDto dto) {
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

    


}