package com.laboccaz.devis.controller;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import com.laboccaz.devis.service.QuoteRequestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quotes")
@CrossOrigin(origins = "http://localhost:5173")
public class QuoteRequestController {

    private final QuoteRequestService service;

    public QuoteRequestController(QuoteRequestService service) {
        this.service = service;
    }

    @PostMapping
    public String createQuoteRequest(@RequestBody QuoteRequestCreateDto dto) {
        return service.createQuoteRequest(dto);
    }

    @GetMapping
    public String getAllQuoteRequests() {
        return service.getAllQuoteRequests();
    }

    @GetMapping("/{id}")
    public String getQuoteRequestById(@PathVariable String id) {
        return service.getQuoteRequestById(id);
    }

    @PatchMapping("/{id}/status")
    public String updateStatus(
            @PathVariable String id,
            @RequestParam String status) {

        return service.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public String deleteQuoteRequest(@PathVariable String id) {
        return service.deleteQuoteRequest(id);
    }

    @PutMapping("/{id}")
    public String updateQuoteRequest(
            @PathVariable String id,
            @RequestBody QuoteRequestCreateDto dto) {
        return service.updateQuoteRequest(id, dto);
    }


    

}