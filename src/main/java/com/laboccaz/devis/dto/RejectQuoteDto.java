package com.laboccaz.devis.dto;

import com.laboccaz.devis.entity.RefusalReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectQuoteDto {

    private RefusalReason refusalReason;
    private String refusalReasonCustom;
}