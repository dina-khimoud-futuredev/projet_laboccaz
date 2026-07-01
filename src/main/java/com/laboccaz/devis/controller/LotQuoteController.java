package com.laboccaz.devis.controller;

import com.laboccaz.devis.dto.LotQuoteCreateDto;
import com.laboccaz.devis.service.LotQuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotes/lot")
@CrossOrigin(origins = "http://localhost:5173")
public class LotQuoteController {

    private final LotQuoteService lotQuoteService;

    public LotQuoteController(LotQuoteService lotQuoteService) {
        this.lotQuoteService = lotQuoteService;
    }

    /**
     * POST /api/quotes/lot
     * Crée un nouveau devis LOT dans Bubble (type Commande).
     * Body : LotQuoteCreateDto (client + infos lot + lignes articles + totaux)
     */
    @PostMapping
    public ResponseEntity<String> createLotQuote(@RequestBody LotQuoteCreateDto dto) {
        String result = lotQuoteService.createLotQuote(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<String> getLotQuotes() {
        String result = lotQuoteService.getLotQuotes();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getLotQuoteById(@PathVariable String id) {
        String result = lotQuoteService.getLotQuoteById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/enriched")
    public ResponseEntity<List<Map<String, Object>>> getLotQuotesEnriched() {
        List<Map<String, Object>> result = lotQuoteService.getLotQuotesEnriched();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/enriched/{id}")
    public ResponseEntity<Map<String, Object>> getLotQuoteEnrichedById(@PathVariable String id) {
        Map<String, Object> result = lotQuoteService.getLotQuoteEnrichedById(id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLotQuote(@PathVariable String id) {
        String result = lotQuoteService.deleteLotQuote(id);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateLotQuoteStatus(
            @PathVariable String id,
            @RequestParam String status) {
        String result = lotQuoteService.updateLotQuoteStatus(id, status);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<String> archiveLotQuote(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String archiveReason = body.get("archiveReason");
        String archiveReasonCustom = body.get("archiveReasonCustom");
        String result = lotQuoteService.archiveLotQuote(id, archiveReason, archiveReasonCustom);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectLotQuote(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String refusalReason = body.get("refusalReason");
        String refusalReasonCustom = body.get("refusalReasonCustom");
        String result = lotQuoteService.rejectLotQuote(id, refusalReason, refusalReasonCustom);
        return ResponseEntity.ok(result);
    }
}