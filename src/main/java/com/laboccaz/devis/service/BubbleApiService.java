package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.LotLineDto;
import com.laboccaz.devis.dto.LotQuoteCreateDto;
import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

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

                body.put("created_from_backend", dto.getCreatedFromBackend());
                body.put("created_by_admin_name", dto.getCreatedByAdminName());

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

        /**
         * Patch partiel d'une demande Bubble : GET complet → merge des champs → PUT.
         * Utilisé pour l'archivage (status + motifs) sans écraser les autres champs.
         */
        public String patchQuoteFields(String id, Map<String, Object> fields) {
                String url = bubbleBaseUrl + "/Demande_devis/" + id;

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 1. Récupérer l'objet complet depuis Bubble
                HttpEntity<String> getEntity = new HttpEntity<>(headers);
                ResponseEntity<Map> getResponse = restTemplate.exchange(
                                url, HttpMethod.GET, getEntity, Map.class);

                Map<String, Object> currentBody = getResponse.getBody();
                Map<String, Object> responseMap = new java.util.LinkedHashMap<>(
                                (Map<String, Object>) currentBody.get("response"));

                // 2. Appliquer les champs à modifier
                responseMap.putAll(fields);

                // 3. Supprimer les champs système Bubble
                responseMap.remove("_id");
                responseMap.remove("Created Date");
                responseMap.remove("Modified Date");
                responseMap.remove("Created By");

                // 4. Remettre l'objet complet via PUT
                HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(responseMap, headers);
                ResponseEntity<String> response = restTemplate.exchange(
                                url, HttpMethod.PUT, putEntity, String.class);

                return response.getBody();
        }

        public String createLotQuote(LotQuoteCreateDto dto) {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // ── ÉTAPE 1 : Créer la Commande sans les lots ──
                Map<String, Object> commandeBody = new HashMap<>();
                commandeBody.put("Statut", "Demande envoyée");

                if (dto.getTotalTtc() != null)
                        commandeBody.put("Total", dto.getTotalTtc());
                if (dto.getTva() != null)
                        commandeBody.put("tva", dto.getTva());
                if (dto.getVendeur() != null)
                        commandeBody.put("Vendeur", dto.getVendeur());

                System.out.println("=== ÉTAPE 1 : Création Commande ===");
                System.out.println("COMMANDE BODY = " + commandeBody);

                HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(commandeBody, headers);

                ResponseEntity<Map> createResponse = restTemplate.exchange(
                                bubbleBaseUrl + "/Commande",
                                HttpMethod.POST,
                                createEntity,
                                Map.class);

                String commandeId = createResponse.getBody().get("id").toString();
                System.out.println("Commande créée avec ID = " + commandeId);

                // ── ÉTAPE 2 : Créer chaque article_quantité_lot ──
                List<String> lotIds = new ArrayList<>();
                List<String> lotAvecQuantiIds = new ArrayList<>();

                System.out.println("=== ÉTAPE 2 : Création des lots ===");
                System.out.println("Nombre de lignes = " + (dto.getLines() != null ? dto.getLines().size() : 0));

                if (dto.getLines() != null) {
                        for (LotLineDto line : dto.getLines()) {

                                System.out.println("--- Ligne article : " + line.getArticleId());

                                if (line.getArticleId() != null && !line.getArticleId().isBlank()) {
                                        lotIds.add(line.getArticleId());
                                }

                                Double totalHtLigne = null;
                                if (line.getUnitPriceHt() != null && line.getQuantity() != null) {
                                        totalHtLigne = line.getUnitPriceHt() * line.getQuantity();
                                }

                                Map<String, Object> lotBody = new HashMap<>();
                                lotBody.put("Article", line.getArticleId());
                                lotBody.put("Commande", commandeId);
                                lotBody.put("Quantit\u00E9", line.getQuantity());
                                if (totalHtLigne != null)
                                        lotBody.put("Total HT", totalHtLigne);

                                URI lotUri;
                                try {
                                        lotUri = new URI(bubbleBaseUrl + "/article_quantit%C3%A9_lot");
                                } catch (java.net.URISyntaxException e) {
                                        throw new RuntimeException("URI invalide", e);
                                }

                                System.out.println("LOT URI = " + lotUri);
                                System.out.println("LOT BODY = " + lotBody);

                                HttpEntity<Map<String, Object>> lotEntity = new HttpEntity<>(lotBody, headers);

                                try {
                                        ResponseEntity<Map> lotResponse = restTemplate.exchange(
                                                        lotUri,
                                                        HttpMethod.POST,
                                                        lotEntity,
                                                        Map.class);

                                        String lotQuantiId = lotResponse.getBody().get("id").toString();
                                        lotAvecQuantiIds.add(lotQuantiId);
                                        System.out.println("article_quantité_lot créé avec ID = " + lotQuantiId);

                                } catch (org.springframework.web.client.HttpClientErrorException e) {
                                        System.out.println(
                                                        "Erreur création lot ligne : " + e.getResponseBodyAsString());
                                        System.out.println("Erreur status : " + e.getStatusCode());
                                        throw e;
                                }
                        }
                }

                // ── ÉTAPE 3 : Patch la Commande ──
                Map<String, Object> patchBody = new HashMap<>();
                if (!lotIds.isEmpty())
                        patchBody.put("Lot", lotIds);
                if (!lotAvecQuantiIds.isEmpty())
                        patchBody.put("Lot_avec_quantit\u00E9", lotAvecQuantiIds);

                System.out.println("PATCH BODY = " + patchBody);

                HttpEntity<Map<String, Object>> patchEntity = new HttpEntity<>(patchBody, headers);

                // RestTemplate spécial qui supporte PATCH
                org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory = new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
                RestTemplate patchRestTemplate = new RestTemplate(factory);

                ResponseEntity<String> patchResponse = patchRestTemplate.exchange(
                                bubbleBaseUrl + "/Commande/" + commandeId,
                                HttpMethod.PATCH,
                                patchEntity,
                                String.class);

                System.out.println("Commande patchée = " + patchResponse.getBody());
                return patchResponse.getBody();
        }
}