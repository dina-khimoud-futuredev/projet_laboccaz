package com.laboccaz.devis.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laboccaz.devis.service.BubbleApiService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final BubbleApiService bubbleApiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthController(BubbleApiService bubbleApiService) {
        this.bubbleApiService = bubbleApiService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) throws Exception {
        String email = body.get("email");
        String password = body.get("password");

        String bubbleResponse = bubbleApiService.getAdminByEmail(email);

        JsonNode root = objectMapper.readTree(bubbleResponse);
        JsonNode results = root.path("response").path("results");

        Map<String, Object> response = new HashMap<>();

        if (results.isEmpty()) {
            response.put("success", false);
            response.put("message", "Utilisateur introuvable");
            return response;
        }

        JsonNode admin = results.get(0);

        String activeValue = admin.path("active").asText();
        boolean active = activeValue.equalsIgnoreCase("yes") || activeValue.equalsIgnoreCase("true");
        String passwordHash = admin.path("password_hash").asText();
        String firstName = admin.path("first_name").asText();

        if (!active) {
            response.put("success", false);
            response.put("message", "Compte désactivé");
            return response;
        }

        if (password.equals(passwordHash)) {
            response.put("success", true);
            response.put("firstName", firstName);
            return response;
        }

        response.put("success", false);
        response.put("message", "Mot de passe incorrect");
        return response;
    }
}