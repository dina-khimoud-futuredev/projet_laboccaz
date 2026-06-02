package com.laboccaz.devis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "quote_requests")
@Getter
@Setter
public class QuoteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientName;

    private String clientEmail;

    private String clientPhone;

    private String companyName;

    private String productReference;

    private Integer quantity;

    private String bubbleArticleId;

    @Column(columnDefinition = "TEXT")
    private String requestDescription;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;

    private String source;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;



    private String productName;
    private Double unitPriceHt;


  

   
}