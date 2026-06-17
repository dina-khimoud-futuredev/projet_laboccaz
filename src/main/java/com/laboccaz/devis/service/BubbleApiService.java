package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;



@Service
public class BubbleApiService {

        @Value("${bubble.api.base-url}")
        private String bubbleBaseUrl;

        @Value("${bubble.api.token}")
        private String bubbleApiToken;

        /*
         * public String getArticles() {
         * String url = bubbleBaseUrl + "/article";
         * 
         * RestTemplate restTemplate = new RestTemplate();
         * 
         * HttpHeaders headers = new HttpHeaders();
         * headers.setBearerAuth(bubbleApiToken);
         * headers.setContentType(MediaType.APPLICATION_JSON);
         * 
         * HttpEntity<String> entity = new HttpEntity<>(headers);
         * 
         * ResponseEntity<String> response = restTemplate.exchange(
         * url,
         * HttpMethod.GET,
         * entity,
         * String.class);
         * return response.getBody();
         * }
         */
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
                                String.class);
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
                                String.class);
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
                                String.class);
                return response.getBody();
        }

        public String updateQuoteRequestStatus(String id, String status) {
                String url = bubbleBaseUrl + "/Demande_devis/" + id;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 1. Récupérer la demande actuelle complète
                HttpEntity<String> getEntity = new HttpEntity<>(headers);

                ResponseEntity<Map> getResponse = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                getEntity,
                                Map.class);

                Map<String, Object> currentBody = getResponse.getBody();

                Map<String, Object> responseMap = (Map<String, Object>) currentBody.get("response");

                // 2. Modifier seulement le statut
                responseMap.put("status", status);

                // 3. Supprimer les champs système Bubble
                responseMap.remove("_id");
                responseMap.remove("Created Date");
                responseMap.remove("Modified Date");
                responseMap.remove("Created By");

                // 4. Réenvoyer toute la demande avec le nouveau statut
                HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(responseMap, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.PUT,
                                putEntity,
                                String.class);

                return response.getBody();
        }

        public String getArticleBySlug(String slug) {

                String constraints = "[{\"key\":\"Slug\",\"constraint_type\":\"equals\",\"value\":\""
                                + slug
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/Article")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                entity,
                                String.class);

                return response.getBody();
        }

        public String getArticles() {
                String url = bubbleBaseUrl + "/Article";

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                String.class);

                return response.getBody();
        }

        public String deleteQuoteRequest(String id) {
                String url = bubbleBaseUrl + "/Demande_devis/" + id;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.DELETE,
                                entity,
                                String.class);

                return response.getBody();
        }

        public String updateQuoteRequest(String id, QuoteRequestCreateDto dto) {
                String url = bubbleBaseUrl + "/Demande_devis/" + id;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();

                body.put("client_name", dto.getClientName());
                body.put("client_email", dto.getClientEmail());
                body.put("client_phone", dto.getClientPhone());
                body.put("company_name", dto.getCompanyName());
                body.put("quantity", dto.getQuantity());
                body.put("request_description", dto.getRequestDescription());

                body.put("article", dto.getBubbleArticleId());
                body.put("article_id", dto.getBubbleArticleId());

                body.put("product_name", dto.getProductName());
                body.put("product_reference", dto.getProductReference());
                body.put("unit_price_ht", dto.getUnitPriceHt());

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.PUT,
                                entity,
                                String.class);

                return response.getBody();

        }

        public String getAdminByEmail(String email) {
                String constraints = "[{\"key\":\"email\",\"constraint_type\":\"equals\",\"value\":\""
                                + email
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/Admin_Devis")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                entity,
                                String.class);

                return response.getBody();
        }

        public String getArticleById(String id) {
                String url = bubbleBaseUrl + "/Article/" + id;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<String> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                String.class);

                return response.getBody();
        }

        public String searchClient(String email, String companyName) {
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                String constraints = null;

                if (email != null && !email.isBlank()) {
                        constraints = "[{\"key\":\"email_text\",\"constraint_type\":\"equals\",\"value\":\""
                                        + email.trim()
                                        + "\"}]";
                } else if (companyName != null && !companyName.isBlank()) {
                        constraints = "[{\"key\":\"Raison sociale\",\"constraint_type\":\"equals\",\"value\":\""
                                        + companyName.trim()
                                        + "\"}]";
                }

                URI uri;

                if (constraints != null) {
                        uri = UriComponentsBuilder
                                        .fromHttpUrl(bubbleBaseUrl + "/User")
                                        .queryParam("constraints", constraints)
                                        .build(false)
                                        .toUri();
                } else {
                        uri = URI.create(bubbleBaseUrl + "/User");
                }

                System.out.println("Constraints = " + constraints);
                System.out.println("URI = " + uri);

                ResponseEntity<String> response = restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                entity,
                                String.class);

                return response.getBody();

        }

        public Map<String, Object> getBubbleUserById(String bubbleUserId) {
                String url = bubbleBaseUrl + "/User/" + bubbleUserId;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                Map.class);

                return (Map<String, Object>) response.getBody().get("response");
        }

        public Map<String, Object> searchBubbleUserByEmailText(String email) {
                String constraints = "[{\"key\":\"email_text\",\"constraint_type\":\"equals\",\"value\":\""
                                + email
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/User")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                return getFirstBubbleUser(uri);
        }

        public Map<String, Object> searchBubbleUserByCompanyName(String companyName) {
                String constraints = "[{\"key\":\"Nom de l’entreprise\",\"constraint_type\":\"equals\",\"value\":\""
                                + companyName
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/User")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                Map<String, Object> user = getFirstBubbleUser(uri);

                if (user != null) {
                        return user;
                }

                String constraintsRaisonSociale = "[{\"key\":\"Raison sociale\",\"constraint_type\":\"equals\",\"value\":\""
                                + companyName
                                + "\"}]";

                URI uriRaisonSociale = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/User")
                                .queryParam("constraints", constraintsRaisonSociale)
                                .build()
                                .toUri();

                return getFirstBubbleUser(uriRaisonSociale);
        }

        private Map<String, Object> getFirstBubbleUser(URI uri) {
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                uri,
                                HttpMethod.GET,
                                entity,
                                Map.class);

                Map<String, Object> body = response.getBody();
                Map<String, Object> responseMap = (Map<String, Object>) body.get("response");

                java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) responseMap
                                .get("results");

                if (results == null || results.isEmpty()) {
                        return null;
                }

                return results.get(0);
        }

        public Map<String, Object> searchBubbleExternalClientByEmail(String email) {
                String constraints = "[{\"key\":\"email\",\"constraint_type\":\"equals\",\"value\":\""
                                + email
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/Client_externe")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                return getFirstBubbleUser(uri);
        }

        public Map<String, Object> searchBubbleExternalClientByCompanyName(String companyName) {
                String constraints = "[{\"key\":\"raison_sociale\",\"constraint_type\":\"equals\",\"value\":\""
                                + companyName
                                + "\"}]";

                URI uri = UriComponentsBuilder
                                .fromHttpUrl(bubbleBaseUrl + "/Client_externe")
                                .queryParam("constraints", constraints)
                                .build()
                                .toUri();

                return getFirstBubbleUser(uri);
        }

        public Map<String, Object> createBubbleExternalClient(String fullName, String email, String phone,
                        String companyName) {
                String url = bubbleBaseUrl + "/Client_externe";

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> body = new HashMap<>();
                body.put("full_name", fullName);
                body.put("email", email);
                body.put("phone", phone);
                body.put("raison_sociale", companyName);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url,
                                HttpMethod.POST,
                                entity,
                                Map.class);

                Map<String, Object> responseBody = response.getBody();
                String createdId = responseBody.get("id").toString();

                return getBubbleExternalClientById(createdId);
        }

        public Map<String, Object> getBubbleExternalClientById(String externalClientId) {
                String url = bubbleBaseUrl + "/Client_externe/" + externalClientId;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                Map.class);

                return (Map<String, Object>) response.getBody().get("response");
        }

}