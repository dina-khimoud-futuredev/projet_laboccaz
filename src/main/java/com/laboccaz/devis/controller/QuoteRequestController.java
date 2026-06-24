package com.laboccaz.devis.controller;

import com.laboccaz.devis.dto.ArchiveQuoteDto;
import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import com.laboccaz.devis.service.QuoteRequestService;
import org.springframework.web.bind.annotation.*;

import com.laboccaz.devis.dto.RejectQuoteDto;

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

    /**
     * Archivage avec motif obligatoire (prédéfini ou libre).
     * POST /api/quotes/{id}/archive
     * Body : { "archiveReason": "CONCURRENT_CHOISI", "archiveReasonCustom": "" }
     * ou { "archiveReason": "AUTRE", "archiveReasonCustom": "Mon motif
     * personnalisé" }
     */
    @PostMapping("/{id}/archive")
    public String archiveQuote(
            @PathVariable String id,
            @RequestBody ArchiveQuoteDto dto) {
        return service.archiveQuote(id, dto);
    }

    @PostMapping("/{id}/reject")
    public String rejectQuote(
            @PathVariable String id,
            @RequestBody RejectQuoteDto dto) {
        return service.rejectQuote(id, dto);
    }
}