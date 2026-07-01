package com.laboccaz.devis.controller;

import com.laboccaz.devis.service.BubbleApiService;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;

@RestController
@RequestMapping("/api/bubble")
@CrossOrigin(origins = "http://localhost:5173")
public class BubbleController {

    private final BubbleApiService bubbleApiService;

    public BubbleController(BubbleApiService bubbleApiService) {
        this.bubbleApiService = bubbleApiService;
    }

    @GetMapping("/articles")
    public String getArticles() {
        return bubbleApiService.getArticles();
    }

    @GetMapping("/articles/enriched") // ← AJOUTÉ, avant /{id}
    public List<Map<String, Object>> getArticlesEnriched() {
        return bubbleApiService.getArticlesEnriched();
    }

    @GetMapping("/articles/slug/{slug}")
    public String getArticleBySlug(@PathVariable String slug) {
        return bubbleApiService.getArticleBySlug(slug);
    }

    @GetMapping("/articles/{id}") // ← reste EN DERNIER
    public String getArticleById(@PathVariable String id) {
        return bubbleApiService.getArticleById(id);
    }

    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, byte[].class);
            MediaType contentType = response.getHeaders().getContentType();
            return ResponseEntity
                    .ok()
                    .contentType(contentType != null ? contentType : MediaType.IMAGE_JPEG)
                    .body(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/clients/search")
    public String searchClient(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String companyName) {
        return bubbleApiService.searchClient(email, companyName);
    }
}