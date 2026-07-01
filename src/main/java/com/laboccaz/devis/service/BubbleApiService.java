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
               // body.put("client_address", dto.getClientAddress());

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

        @SuppressWarnings("unchecked")
        public String getQuoteRequestById(String id) {

                String url = bubbleBaseUrl + "/Demande_devis/" + id;
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url, HttpMethod.GET, entity, Map.class);

                Map<String, Object> body = response.getBody();
                if (body == null)
                        return "{}";

                Map<String, Object> responseMap = (Map<String, Object>) body.get("response");
                if (responseMap == null)
                        return "{}";

                // Enrichir avec l'adresse du client depuis la table User via client_email
                String clientAddress = null;
                Object clientEmail = responseMap.get("client_email");
                if (clientEmail != null && !clientEmail.toString().isBlank()) {
                        try {
                                Map<String, Object> user = searchBubbleUserByEmailText(clientEmail.toString());
                                if (user != null && user.get("Adresse") != null) {
                                        Object adresseObj = user.get("Adresse");
                                        if (adresseObj instanceof Map) {
                                                // Bubble stocke l'adresse comme un objet {address, lat, lng}
                                                Object addressText = ((Map<?, ?>) adresseObj).get("address");
                                                clientAddress = addressText != null ? addressText.toString() : null;
                                        } else {
                                                clientAddress = adresseObj.toString();
                                        }
                                }
                        } catch (Exception e) {
                                System.out.println("Erreur récupération adresse client : " + e.getMessage());
                        }
                }
                responseMap.put("client_address", clientAddress);

                // Retourner le JSON enrichi
                try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        return mapper.writeValueAsString(java.util.Map.of("response", responseMap));
                } catch (Exception e) {
                        return "{}";
                }
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

        // ── Récupérer un objet Référence par son ID dans la table Référence de Bubble
        // ──
        @SuppressWarnings("unchecked")
        public Map<String, Object> getReferenceById(String refId) {
                String url = bubbleBaseUrl + "/R%C3%A9f%C3%A9rence/" + refId;

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(
                                url, HttpMethod.GET, entity, Map.class);

                Map<String, Object> body = response.getBody();
                if (body == null)
                        return new HashMap<>();
                Map<String, Object> responseMap = (Map<String, Object>) body.get("response");
                return responseMap != null ? responseMap : new HashMap<>();
        }

        // ── Retourner les articles enrichis avec les données de leur Référence liée ──
        @SuppressWarnings("unchecked")
        public List<Map<String, Object>> getArticlesEnriched() {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // 1. Récupérer TOUS les articles avec pagination (100 par page)
                List<Map<String, Object>> results = new ArrayList<>();
                int cursor = 0;
                int pageSize = 100;
                while (true) {
                        String url = bubbleBaseUrl + "/Article?limit=" + pageSize + "&cursor=" + cursor;
                        ResponseEntity<Map> articlesResponse = restTemplate.exchange(url, HttpMethod.GET, entity,
                                        Map.class);
                        Map<String, Object> articlesBody = articlesResponse.getBody();
                        if (articlesBody == null)
                                break;
                        Map<String, Object> articlesResponseMap = (Map<String, Object>) articlesBody.get("response");
                        if (articlesResponseMap == null)
                                break;
                        List<Map<String, Object>> page = (List<Map<String, Object>>) articlesResponseMap.get("results");
                        if (page == null || page.isEmpty())
                                break;
                        results.addAll(page);
                        Integer remaining = (Integer) articlesResponseMap.get("remaining");
                        if (remaining == null || remaining == 0)
                                break;
                        cursor += pageSize;
                }
                if (results.isEmpty())
                        return new ArrayList<>();

                // 2. Récupérer toutes les Références avec pagination
                Map<String, Map<String, Object>> refMap = new HashMap<>();
                try {
                        int refCursor = 0;
                        while (true) {
                                URI refUri = new URI(
                                                bubbleBaseUrl + "/R%C3%A9f%C3%A9rence?limit=100&cursor=" + refCursor);
                                ResponseEntity<Map> refsResponse = restTemplate.exchange(
                                                refUri, HttpMethod.GET, entity, Map.class);
                                Map<String, Object> refsBody = refsResponse.getBody();
                                if (refsBody == null)
                                        break;
                                Map<String, Object> refsResponseMap = (Map<String, Object>) refsBody.get("response");
                                if (refsResponseMap == null)
                                        break;
                                List<Map<String, Object>> refs = (List<Map<String, Object>>) refsResponseMap
                                                .get("results");
                                if (refs == null || refs.isEmpty())
                                        break;
                                for (Map<String, Object> ref : refs) {
                                        String refId = ref.get("_id") != null ? ref.get("_id").toString() : null;
                                        if (refId != null)
                                                refMap.put(refId, ref);
                                }
                                Integer refsRemaining = (Integer) refsResponseMap.get("remaining");
                                if (refsRemaining == null || refsRemaining == 0)
                                        break;
                                refCursor += 100;
                        }
                } catch (Exception e) {
                        System.out.println("Erreur chargement table Référence : " + e.getMessage());
                }

                // 3. Jointure en mémoire — aucun appel supplémentaire
                for (Map<String, Object> article : results) {
                        Object refId = article.get("Référence");
                        if (refId != null && !refId.toString().isBlank()) {
                                Map<String, Object> ref = refMap.get(refId.toString());
                                if (ref != null) {
                                        article.put("ref_reference", ref.get("Référence"));
                                        article.put("ref_modele", ref.get("Modèle"));
                                        article.put("ref_titre", ref.get("Titre"));
                                        article.put("ref_description", ref.get("Description"));
                                        article.put("ref_caracteristiques", ref.get("caracteristiques"));
                                        article.put("ref_image", ref.get("Image"));
                                }
                        }
                }

                return results;
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
                        String companyName, String address) {
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
                if (address != null && !address.isBlank()) {
                        body.put("Adresse", address);
                }

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
                commandeBody.put("admin_status", "RECEIVED");
                commandeBody.put("created_from_backend", true);

                // Infos client
                if (dto.getBubbleClientId() != null && !dto.getBubbleClientId().isBlank())
                        commandeBody.put("Client", dto.getBubbleClientId());
                if (dto.getClientName() != null)
                        commandeBody.put("client_name", dto.getClientName());
                if (dto.getClientEmail() != null)
                        commandeBody.put("client_email", dto.getClientEmail());
                if (dto.getClientPhone() != null)
                        commandeBody.put("client_phone", dto.getClientPhone());
                if (dto.getCompanyName() != null)
                        commandeBody.put("company_name", dto.getCompanyName());
                if (dto.getCreatedByAdminName() != null)
                        commandeBody.put("created_by_admin_name", dto.getCreatedByAdminName());

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

        public String getCommandes() {
                String url = bubbleBaseUrl + "/Commande";

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

        public String getCommandeById(String id) {
                String url = bubbleBaseUrl + "/Commande/" + id;

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

        // ── Détermine le vendeur du lot : un seul vendeur ou "Plusieurs vendeurs" ──
        @SuppressWarnings("unchecked")
        private Map<String, Object> calculateLotVendeurInfo(Object lotIds,
                        Map<String, Map<String, Object>> articleCache,
                        Map<String, Map<String, Object>> userCache) {
                Map<String, Object> result = new HashMap<>();

                if (!(lotIds instanceof List)) {
                        return result;
                }

                List<String> idList = (List<String>) lotIds;
                java.util.Set<String> vendeurIds = new java.util.LinkedHashSet<>();

                for (String articleId : idList) {
                        if (articleId == null || articleId.isBlank())
                                continue;
                        try {
                                Map<String, Object> article = articleCache.computeIfAbsent(articleId, aid -> {
                                        String url = bubbleBaseUrl + "/Article/" + aid;
                                        RestTemplate rt = new RestTemplate();
                                        HttpHeaders h = new HttpHeaders();
                                        h.setBearerAuth(bubbleApiToken);
                                        HttpEntity<String> e = new HttpEntity<>(h);
                                        try {
                                                ResponseEntity<Map> resp = rt.exchange(url, HttpMethod.GET, e,
                                                                Map.class);
                                                Map<String, Object> b = resp.getBody();
                                                return b != null ? (Map<String, Object>) b.get("response")
                                                                : new HashMap<>();
                                        } catch (Exception ex) {
                                                return new HashMap<>();
                                        }
                                });
                                Object vendeurId = article.get("vendeur");
                                if (vendeurId != null && !vendeurId.toString().isBlank()) {
                                        vendeurIds.add(vendeurId.toString());
                                }
                        } catch (Exception e) {
                                System.out.println("Erreur récupération article " + articleId + " : " + e.getMessage());
                        }
                }

                if (vendeurIds.isEmpty()) {
                        return result;
                } else if (vendeurIds.size() == 1) {
                        String vendeurId = vendeurIds.iterator().next();
                        try {
                                Map<String, Object> vendeur = userCache.computeIfAbsent(vendeurId,
                                                this::getBubbleUserById);
                                String name = vendeur.get("First Last") != null
                                                ? vendeur.get("First Last").toString()
                                                : ((vendeur.get("First") != null ? vendeur.get("First") : "") + " " +
                                                                (vendeur.get("Last") != null ? vendeur.get("Last")
                                                                                : ""))
                                                                .trim();
                                String email = vendeur.get("email_text") != null
                                                ? vendeur.get("email_text").toString()
                                                : null;
                                result.put("id", vendeurId);
                                result.put("name", name);
                                result.put("email", email);
                                result.put("multiple", false);
                        } catch (Exception e) {
                                System.out.println("Erreur récupération vendeur unique " + vendeurId + " : "
                                                + e.getMessage());
                        }
                } else {
                        result.put("name", "Plusieurs vendeurs");
                        result.put("multiple", true);
                }

                return result;
        }

        public Map<String, Object> getCommandeEnriched(String id) {
                // Récupérer la commande
                String url = bubbleBaseUrl + "/Commande/" + id;
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map<String, Object> commande = new java.util.LinkedHashMap<>(
                                (Map<String, Object>) response.getBody().get("response"));

                // Enrichir avec le vendeur
                Object vendeurId = commande.get("Vendeur");
                if (vendeurId != null && !vendeurId.toString().isBlank()) {
                        try {
                                Map<String, Object> vendeur = getBubbleUserById(vendeurId.toString());
                                Map<String, Object> vendeurInfo = new HashMap<>();
                                vendeurInfo.put("id", vendeurId);
                                vendeurInfo.put("name", vendeur.get("First Last") != null ? vendeur.get("First Last")
                                                : ((vendeur.get("First") != null ? vendeur.get("First") : "") + " " +
                                                                (vendeur.get("Last") != null ? vendeur.get("Last")
                                                                                : ""))
                                                                .trim());
                                vendeurInfo.put("email", vendeur.get("email_text") != null ? vendeur.get("email_text")
                                                : ((Map) ((Map) vendeur.getOrDefault("authentication", new HashMap<>()))
                                                                .getOrDefault("email", new HashMap<>())).get("email"));
                                commande.put("vendeur_info", vendeurInfo);
                        } catch (Exception e) {
                                System.out.println("Erreur récupération vendeur : " + e.getMessage());
                        }
                } else {
                        // Pas de champ Vendeur direct (commande admin) → déduire depuis les articles du
                        // lot
                        Map<String, Map<String, Object>> articleCache = new HashMap<>();
                        Map<String, Map<String, Object>> userCacheVendeur = new HashMap<>();
                        Map<String, Object> lotVendeurInfo = calculateLotVendeurInfo(
                                        commande.get("Lot"), articleCache, userCacheVendeur);
                        if (!lotVendeurInfo.isEmpty()) {
                                commande.put("vendeur_info", lotVendeurInfo);
                        }
                }

                // Enrichir avec le créateur
                Boolean createdFromBackend = (Boolean) commande.get("created_from_backend");
                if (createdFromBackend != null && createdFromBackend) {
                        // Admin → on a déjà created_by_admin_name
                        Map<String, Object> creatorInfo = new HashMap<>();
                        creatorInfo.put("type", "admin");
                        creatorInfo.put("name", commande.getOrDefault("created_by_admin_name", "Admin"));
                        commande.put("creator_info", creatorInfo);
                } else {
                        // Client → récupérer depuis Created By
                        Object createdById = commande.get("Created By");
                        if (createdById != null && !createdById.toString().isBlank()) {
                                try {
                                        Map<String, Object> creator = getBubbleUserById(createdById.toString());
                                        Map<String, Object> creatorInfo = new HashMap<>();
                                        creatorInfo.put("type", "client");
                                        creatorInfo.put("name", creator.get("First Last") != null
                                                        ? creator.get("First Last")
                                                        : ((creator.get("First") != null ? creator.get("First") : "")
                                                                        + " " +
                                                                        (creator.get("Last") != null
                                                                                        ? creator.get("Last")
                                                                                        : ""))
                                                                        .trim());
                                        creatorInfo.put("email", creator.get("email_text"));
                                        commande.put("creator_info", creatorInfo);
                                } catch (Exception e) {
                                        System.out.println("Erreur récupération créateur : " + e.getMessage());
                                }
                        }
                }

                // Enrichir client si les champs texte sont absents
                String clientName = (String) commande.get("client_name");
                if (clientName == null || clientName.isBlank()) {
                        Object clientUserId = commande.get("Client");
                        Object createdById = commande.get("Created By");
                        String userIdToFetch = (clientUserId != null && !clientUserId.toString().isBlank())
                                        ? clientUserId.toString()
                                        : (createdById != null && !createdById.toString().isBlank()
                                                        ? createdById.toString()
                                                        : null);
                        if (userIdToFetch != null) {
                                try {
                                        Map<String, Object> user = getBubbleUserById(userIdToFetch);
                                        String fullName = user.get("First Last") != null
                                                        ? user.get("First Last").toString()
                                                        : ((user.get("First") != null ? user.get("First") : "") + " " +
                                                                        (user.get("Last") != null ? user.get("Last")
                                                                                        : ""))
                                                                        .trim();
                                        String email = user.get("email_text") != null
                                                        ? user.get("email_text").toString()
                                                        : null;
                                        String phone = user.get("Téléphone") != null
                                                        ? user.get("Téléphone").toString()
                                                        : null;
                                        String company = user.get("Raison sociale") != null
                                                        ? user.get("Raison sociale").toString()
                                                        : (user.get("Nom de l'entreprise") != null
                                                                        ? user.get("Nom de l'entreprise").toString()
                                                                        : null);
                                        Object adresseRaw = user.get("Adresse");
                                        String address = null;
                                        if (adresseRaw instanceof java.util.Map) {
                                                Object addrText = ((java.util.Map<?, ?>) adresseRaw).get("address");
                                                address = addrText != null ? addrText.toString() : null;
                                        } else if (adresseRaw != null) {
                                                address = adresseRaw.toString();
                                        }
                                        commande.put("client_name", fullName);
                                        commande.put("client_email", email);
                                        commande.put("client_phone", phone);
                                        commande.put("company_name", company);
                                        commande.put("client_address", address);
                                } catch (Exception e) {
                                        System.out.println("Erreur récupération client : " + e.getMessage());
                                }
                        }
                }

                // Enrichir chaque article du lot avec son nom et son vendeur
                Object lotIdsObj = commande.get("Lot");
                if (lotIdsObj instanceof java.util.List) {
                        java.util.List<String> lotIdsList = (java.util.List<String>) lotIdsObj;
                        Map<String, Map<String, Object>> articleCacheLocal = new HashMap<>();
                        Map<String, Map<String, Object>> userCacheLocal = new HashMap<>();
                        List<Map<String, Object>> lotArticlesInfo = new ArrayList<>();

                        for (String articleId : lotIdsList) {
                                if (articleId == null || articleId.isBlank())
                                        continue;
                                Map<String, Object> articleInfo = new HashMap<>();
                                articleInfo.put("id", articleId);
                                try {
                                        Map<String, Object> article = articleCacheLocal.computeIfAbsent(articleId,
                                                        aid -> {
                                                                String url2 = bubbleBaseUrl + "/Article/" + aid;
                                                                RestTemplate rt = new RestTemplate();
                                                                HttpHeaders h = new HttpHeaders();
                                                                h.setBearerAuth(bubbleApiToken);
                                                                HttpEntity<String> e = new HttpEntity<>(h);
                                                                try {
                                                                        ResponseEntity<Map> resp = rt.exchange(url2,
                                                                                        HttpMethod.GET, e, Map.class);
                                                                        Map<String, Object> b = resp.getBody();
                                                                        return b != null ? (Map<String, Object>) b
                                                                                        .get("response")
                                                                                        : new HashMap<>();
                                                                } catch (Exception ex) {
                                                                        return new HashMap<>();
                                                                }
                                                        });
                                        articleInfo.put("titre", article.get("Titre") != null ? article.get("Titre")
                                                        : article.get("Nom"));
                                        articleInfo.put("slug", article.get("Slug"));

                                        // Champs directs de la table Article
                                        articleInfo.put("images", article.get("Images"));
                                        articleInfo.put("prix_ht", article.get("Prix HT "));
                                        articleInfo.put("nature_vente", article.get("Nature de la vente"));
                                        articleInfo.put("etat", article.get("Etat"));
                                        articleInfo.put("garantie", article.get("Garantie vendeur ?"));
                                        articleInfo.put("garantie_duree", article.get("Garantie vendeur durée"));
                                        articleInfo.put("description", article.get("Description"));

                                        // Champs enrichis depuis la table Référence liée
                                        Object refId = article.get("Référence");
                                        if (refId != null && !refId.toString().isBlank()) {
                                                try {
                                                        Map<String, Object> ref = getReferenceById(refId.toString());
                                                        if (ref != null && !ref.isEmpty()) {
                                                                articleInfo.put("ref_titre", ref.get("Titre"));
                                                                articleInfo.put("ref_reference", ref.get("Référence"));
                                                                articleInfo.put("ref_modele", ref.get("Modèle"));
                                                                articleInfo.put("ref_caracteristiques",
                                                                                ref.get("caracteristiques"));
                                                                articleInfo.put("ref_description",
                                                                                ref.get("Description"));
                                                        }
                                                } catch (Exception ex) {
                                                        System.out.println("Erreur récupération référence article "
                                                                        + articleId + " : " + ex.getMessage());
                                                }
                                        }

                                        Object vendeurId2 = article.get("vendeur");
                                        if (vendeurId2 != null && !vendeurId2.toString().isBlank()) {
                                                try {
                                                        Map<String, Object> vendeurUser = userCacheLocal
                                                                        .computeIfAbsent(
                                                                                        vendeurId2.toString(),
                                                                                        this::getBubbleUserById);
                                                        String vendeurName = vendeurUser.get("First Last") != null
                                                                        ? vendeurUser.get("First Last").toString()
                                                                        : ((vendeurUser.get("First") != null
                                                                                        ? vendeurUser.get("First")
                                                                                        : "") + " " +
                                                                                        (vendeurUser.get("Last") != null
                                                                                                        ? vendeurUser.get(
                                                                                                                        "Last")
                                                                                                        : ""))
                                                                                        .trim();
                                                        articleInfo.put("vendeur_name", vendeurName);
                                                } catch (Exception ex) {
                                                        System.out.println("Erreur vendeur article " + articleId + " : "
                                                                        + ex.getMessage());
                                                }
                                        }
                                } catch (Exception e) {
                                        System.out.println("Erreur enrichissement article lot " + articleId + " : "
                                                        + e.getMessage());
                                }
                                lotArticlesInfo.add(articleInfo);
                        }
                        commande.put("lot_articles_info", lotArticlesInfo);
                }

                return commande;
        }

        public List<Map<String, Object>> getAllCommandesEnriched() {
                // Récupérer toutes les commandes
                String url = bubbleBaseUrl + "/Commande";
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                Map<String, Object> body = response.getBody();
                Map<String, Object> responseMap = (Map<String, Object>) body.get("response");
                List<Map<String, Object>> commandes = (List<Map<String, Object>>) responseMap.get("results");

                if (commandes == null)
                        return new java.util.ArrayList<>();

                // Cache vendeurs et créateurs pour éviter les appels dupliqués
                Map<String, Map<String, Object>> userCache = new HashMap<>();
                Map<String, Map<String, Object>> articleCacheGlobal = new HashMap<>();

                List<Map<String, Object>> enriched = new java.util.ArrayList<>();

                for (Map<String, Object> commande : commandes) {
                        Map<String, Object> c = new java.util.LinkedHashMap<>(commande);

                        // Enrichir vendeur
                        Object vendeurId = c.get("Vendeur");
                        if (vendeurId != null && !vendeurId.toString().isBlank()) {
                                try {
                                        Map<String, Object> vendeur = userCache.computeIfAbsent(
                                                        vendeurId.toString(), this::getBubbleUserById);
                                        Map<String, Object> vendeurInfo = new HashMap<>();
                                        vendeurInfo.put("id", vendeurId);
                                        vendeurInfo.put("name", vendeur.get("First Last") != null
                                                        ? vendeur.get("First Last")
                                                        : ((vendeur.get("First") != null ? vendeur.get("First") : "")
                                                                        + " " +
                                                                        (vendeur.get("Last") != null
                                                                                        ? vendeur.get("Last")
                                                                                        : ""))
                                                                        .trim());
                                        vendeurInfo.put("email", vendeur.get("email_text") != null
                                                        ? vendeur.get("email_text")
                                                        : ((Map) ((Map) vendeur.getOrDefault("authentication",
                                                                        new HashMap<>()))
                                                                        .getOrDefault("email", new HashMap<>()))
                                                                        .get("email"));
                                        c.put("vendeur_info", vendeurInfo);
                                } catch (Exception e) {
                                        System.out.println("Erreur vendeur " + vendeurId + " : " + e.getMessage());
                                }
                        } else {
                                // Pas de champ Vendeur direct (commande admin) → déduire depuis les articles du
                                // lot
                                Map<String, Object> lotVendeurInfo = calculateLotVendeurInfo(
                                                c.get("Lot"), articleCacheGlobal, userCache);
                                if (!lotVendeurInfo.isEmpty()) {
                                        c.put("vendeur_info", lotVendeurInfo);
                                }
                        }

                        // Enrichir créateur
                        Boolean createdFromBackend = (Boolean) c.get("created_from_backend");
                        if (createdFromBackend != null && createdFromBackend) {
                                Map<String, Object> creatorInfo = new HashMap<>();
                                creatorInfo.put("type", "admin");
                                creatorInfo.put("name", c.getOrDefault("created_by_admin_name", "Admin"));
                                c.put("creator_info", creatorInfo);
                        } else {
                                Object createdById = c.get("Created By");
                                if (createdById != null && !createdById.toString().isBlank()) {
                                        try {
                                                Map<String, Object> creator = userCache.computeIfAbsent(
                                                                createdById.toString(), this::getBubbleUserById);
                                                Map<String, Object> creatorInfo = new HashMap<>();
                                                creatorInfo.put("type", "client");
                                                creatorInfo.put("name", creator.get("First Last") != null
                                                                ? creator.get("First Last")
                                                                : ((creator.get("First") != null ? creator.get("First")
                                                                                : "") + " " +
                                                                                (creator.get("Last") != null
                                                                                                ? creator.get("Last")
                                                                                                : ""))
                                                                                .trim());
                                                creatorInfo.put("email", creator.get("email_text"));
                                                c.put("creator_info", creatorInfo);
                                        } catch (Exception e) {
                                                System.out.println("Erreur créateur " + createdById + " : "
                                                                + e.getMessage());
                                        }
                                }
                        }

                        // Enrichir client si les champs texte sont absents (commande créée depuis
                        // Bubble)
                        String clientName = (String) c.get("client_name");
                        if (clientName == null || clientName.isBlank()) {
                                Object clientUserId2 = c.get("Client");
                                Object createdById = c.get("Created By");
                                String userIdToFetch2 = (clientUserId2 != null && !clientUserId2.toString().isBlank())
                                                ? clientUserId2.toString()
                                                : (createdById != null && !createdById.toString().isBlank()
                                                                ? createdById.toString()
                                                                : null);
                                if (userIdToFetch2 != null) {
                                        try {
                                                Map<String, Object> user = userCache.computeIfAbsent(
                                                                userIdToFetch2, this::getBubbleUserById);
                                                String fullName = user.get("First Last") != null
                                                                ? user.get("First Last").toString()
                                                                : ((user.get("First") != null ? user.get("First") : "")
                                                                                + " " +
                                                                                (user.get("Last") != null
                                                                                                ? user.get("Last")
                                                                                                : ""))
                                                                                .trim();
                                                String email = user.get("email_text") != null
                                                                ? user.get("email_text").toString()
                                                                : null;
                                                String phone = user.get("Téléphone") != null
                                                                ? user.get("Téléphone").toString()
                                                                : null;
                                                String company = user.get("Raison sociale") != null
                                                                ? user.get("Raison sociale").toString()
                                                                : (user.get("Nom de l'entreprise") != null
                                                                                ? user.get("Nom de l'entreprise")
                                                                                                .toString()
                                                                                : null);
                                                Object adresseRaw = user.get("Adresse");
                                                String address = null;
                                                if (adresseRaw instanceof java.util.Map) {
                                                        Object addrText = ((java.util.Map<?, ?>) adresseRaw)
                                                                        .get("address");
                                                        address = addrText != null ? addrText.toString() : null;
                                                } else if (adresseRaw != null) {
                                                        address = adresseRaw.toString();
                                                }
                                                c.put("client_name", fullName);
                                                c.put("client_email", email);
                                                c.put("client_phone", phone);
                                                c.put("company_name", company);
                                                c.put("client_address", address);
                                        } catch (Exception e) {
                                                System.out.println("Erreur client " + createdById + " : "
                                                                + e.getMessage());
                                        }
                                }
                        }

                        enriched.add(c);
                }

                return enriched;
        }

        public String deleteCommande(String id) {
                String url = bubbleBaseUrl + "/Commande/" + id;
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(
                                url, HttpMethod.DELETE, entity, String.class);
                return response.getBody();
        }

        public String patchCommandeFields(String id, Map<String, Object> fields) {
                String url = bubbleBaseUrl + "/Commande/" + id;

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bubbleApiToken);
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 1. Récupérer l'objet complet
                HttpEntity<String> getEntity = new HttpEntity<>(headers);
                ResponseEntity<Map> getResponse = restTemplate.exchange(
                                url, HttpMethod.GET, getEntity, Map.class);

                Map<String, Object> currentBody = getResponse.getBody();
                Map<String, Object> responseMap = new java.util.LinkedHashMap<>(
                                (Map<String, Object>) currentBody.get("response"));

                // 2. Appliquer les champs
                responseMap.putAll(fields);

                // 3. Supprimer les champs système Bubble
                responseMap.remove("_id");
                responseMap.remove("Created Date");
                responseMap.remove("Modified Date");
                responseMap.remove("Created By");

                // 4. PUT avec HttpComponentsClientHttpRequestFactory pour support PATCH
                org.springframework.http.client.HttpComponentsClientHttpRequestFactory factory = new org.springframework.http.client.HttpComponentsClientHttpRequestFactory();
                RestTemplate patchRestTemplate = new RestTemplate(factory);

                HttpEntity<Map<String, Object>> putEntity = new HttpEntity<>(responseMap, headers);
                ResponseEntity<String> response = patchRestTemplate.exchange(
                                url, HttpMethod.PATCH, putEntity, String.class);

                return response.getBody();
        }

}