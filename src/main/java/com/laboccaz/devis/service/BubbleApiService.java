package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class BubbleApiService {

    @Value("${bubble.api.base-url}")
    private String bubbleBaseUrl;

    @Value("${bubble.api.token}")
    private String bubbleApiToken;

    public String getArticles() {
        String url = bubbleBaseUrl + "/article";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bubbleApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }



    public String getQuoteRequests() {
        String url = bubbleBaseUrl + "/Demande_devis";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bubbleApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            String.class
        );
        return response.getBody();
    }














    public String createQuoteRequest(QuoteRequestCreateDto dto) {
        String url = bubbleBaseUrl + "/Demande_devis";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bubbleApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        body.put("client_name", dto.getClientName());
        body.put("client_email", dto.getClientEmail());
        body.put("client_phone", dto.getClientPhone());
        body.put("company_name", dto.getCompanyName());

        body.put("article", dto.getBubbleArticleId());
        body.put("article_id", dto.getBubbleArticleId());

        body.put("product_name", dto.getProductName());
        body.put("product_reference", dto.getProductReference());
        body.put("unit_price_ht", dto.getUnitPriceHt());
        body.put("quantity", dto.getQuantity());

        body.put("request_description", dto.getRequestDescription());

        body.put("status", "RECEIVED");
        body.put("source", dto.getSource());
        body.put("crm_sync_status", "PENDING");
        body.put("created_from_backend", true);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );

        return response.getBody();
    }










    public String getQuoteRequestById(String id) {

        String url = bubbleBaseUrl + "/Demande_devis/" + id;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bubbleApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }



    public String updateQuoteRequestStatus(String id, String status) {
        String url = bubbleBaseUrl + "/Demande_devis/" + id;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bubbleApiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            entity,
            String.class
        );

        return response.getBody();
    }

}