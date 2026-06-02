package com.laboccaz.devis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuoteRequestCreateDto {

    private String clientName;
    private String clientEmail;
    private String clientPhone;

    private String companyName;

    private String productReference;
    private Integer quantity;

    private String requestDescription;

    private String source;

    private String bubbleArticleId;
    private String productName;
    private Double unitPriceHt;
}