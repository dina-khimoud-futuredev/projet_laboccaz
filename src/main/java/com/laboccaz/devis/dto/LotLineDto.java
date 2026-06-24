package com.laboccaz.devis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LotLineDto {
    private String articleId;
    private String productName;
    private String productReference;
    private Double unitPriceHt;
    private Integer quantity;
}