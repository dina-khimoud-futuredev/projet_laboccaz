package com.laboccaz.devis.controller;

import com.laboccaz.devis.dto.LotQuoteCreateDto;
import com.laboccaz.devis.service.LotQuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}