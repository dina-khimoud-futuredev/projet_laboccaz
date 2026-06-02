package com.laboccaz.devis.controller;

import com.laboccaz.devis.service.BubbleApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bubble")
public class BubbleController {

    private final BubbleApiService bubbleApiService;

    public BubbleController(BubbleApiService bubbleApiService) {
        this.bubbleApiService = bubbleApiService;
    }

    @GetMapping("/articles")
    public String getArticles() {
        return bubbleApiService.getArticles();
    }
}