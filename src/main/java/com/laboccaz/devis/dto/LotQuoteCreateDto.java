package com.laboccaz.devis.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class LotQuoteCreateDto {

    // Client
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String companyName;

    // Lot
    private String lotReference;
    private String lotNumber;
    private String addressLivraison;
    private String addressPickup;
    private String vendeur;
    private Double commissionPct;
    private Double tva;
    private String notes;

    // Articles
    private List<LotLineDto> lines;

    // Totaux calculés côté frontend (utilisés pour Bubble)
    private Double totalHt;
    private Double tvaAmount;
    private Double totalTtc;

    // Méta
    private String source;
    private Boolean createdFromBackend;
    private String createdByAdminName;
}