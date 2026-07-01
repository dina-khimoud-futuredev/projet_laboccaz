package com.laboccaz.devis.service;

import com.laboccaz.devis.dto.QuoteRequestCreateDto;
import com.laboccaz.devis.entity.Client;
import com.laboccaz.devis.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class ClientService {

    private static final String SOURCE_USER = "USER";
    private static final String SOURCE_CLIENT_EXTERNE = "CLIENT_EXTERNE";

    private final ClientRepository clientRepository;
    private final BubbleApiService bubbleApiService;

    public ClientService(ClientRepository clientRepository, BubbleApiService bubbleApiService) {
        this.clientRepository = clientRepository;
        this.bubbleApiService = bubbleApiService;
    }

    public Client syncFromBubbleUserId(String bubbleUserId) {
        Map<String, Object> bubbleUser = bubbleApiService.getBubbleUserById(bubbleUserId);
        return upsertFromBubbleUser(bubbleUser);
    }

    public Client syncFromBubbleExternalClientId(String externalClientId) {
        Map<String, Object> externalClient = bubbleApiService.getBubbleExternalClientById(externalClientId);
        return upsertFromBubbleExternalClient(externalClient);
    }

    public Client syncFromCompanyName(String companyName) {
        Map<String, Object> bubbleUser = bubbleApiService.searchBubbleUserByCompanyName(companyName);

        if (bubbleUser != null) {
            return upsertFromBubbleUser(bubbleUser);
        }

        Map<String, Object> externalClient = bubbleApiService.searchBubbleExternalClientByCompanyName(companyName);

        if (externalClient != null) {
            return upsertFromBubbleExternalClient(externalClient);
        }

        return null;
    }

    public Client syncFromEmail(String email) {
        Map<String, Object> bubbleUser = bubbleApiService.searchBubbleUserByEmailText(email);

        if (bubbleUser != null) {
            return upsertFromBubbleUser(bubbleUser);
        }

        Map<String, Object> externalClient = bubbleApiService.searchBubbleExternalClientByEmail(email);

        if (externalClient != null) {
            return upsertFromBubbleExternalClient(externalClient);
        }

        return null;
    }

    public Client upsertFromQuoteDto(QuoteRequestCreateDto dto) {
        Client existingClient = findExistingClient(dto);

        if (existingClient != null) {
            return existingClient;
        }

        Map<String, Object> createdExternalClient = bubbleApiService.createBubbleExternalClient(
                dto.getClientName(),
                dto.getClientEmail(),
                dto.getClientPhone(),
                dto.getCompanyName(),
                dto.getClientAddress());

        return upsertFromBubbleExternalClient(createdExternalClient);
    }

    private Client findExistingClient(QuoteRequestCreateDto dto) {
        if (dto.getClientEmail() != null && !dto.getClientEmail().isBlank()) {
            Map<String, Object> externalClient = bubbleApiService
                    .searchBubbleExternalClientByEmail(dto.getClientEmail());

            if (externalClient != null) {
                return upsertFromBubbleExternalClient(externalClient);
            }
        }

        if (dto.getCompanyName() != null && !dto.getCompanyName().isBlank()) {
            Map<String, Object> externalClient = bubbleApiService
                    .searchBubbleExternalClientByCompanyName(dto.getCompanyName());

            if (externalClient != null) {
                return upsertFromBubbleExternalClient(externalClient);
            }
        }

        return null;
    }

    private Client upsertFromBubbleUser(Map<String, Object> bubbleUser) {
        String bubbleUserId = getString(bubbleUser, "_id");
        String email = extractEmail(bubbleUser);
        String companyName = firstNotBlank(
                getString(bubbleUser, "Nom de l’entreprise"),
                getString(bubbleUser, "Raison sociale"));

        Optional<Client> existing = Optional.empty();

        if (bubbleUserId != null) {
            existing = clientRepository.findByBubbleUserId(bubbleUserId);
        }

        if (existing.isEmpty() && companyName != null) {
            existing = clientRepository.findByCompanyNameIgnoreCase(companyName);
        }

        if (existing.isEmpty() && email != null) {
            existing = clientRepository.findByEmailIgnoreCase(email);
        }

        Client client = existing.orElseGet(Client::new);

        if (client.getCreatedAt() == null) {
            client.setCreatedAt(LocalDateTime.now());
        }

        client.setBubbleUserId(bubbleUserId);
        client.setBubbleExternalClientId(null);
        client.setClientSource(SOURCE_USER);

        client.setEmail(email);
        client.setFirstName(getString(bubbleUser, "First"));
        client.setLastName(getString(bubbleUser, "Last"));
        client.setFullName(getString(bubbleUser, "First Last"));
        client.setCompanyName(companyName);
        client.setPhone(getString(bubbleUser, "Téléphone"));
        client.setAddress(extractAddress(bubbleUser));
        client.setRole(getString(bubbleUser, "⚙️ Role"));
        client.setSellerType(getString(bubbleUser, "type_vendeur"));
        client.setAccountStatus(getString(bubbleUser, "⚙️ Account status"));
        client.setProfileCompleted(getBoolean(bubbleUser, "Profil complété"));
        client.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(client);
    }

    private Client upsertFromBubbleExternalClient(Map<String, Object> externalClient) {
        String externalClientId = getString(externalClient, "_id");
        String email = getString(externalClient, "email");
        String companyName = getString(externalClient, "raison_sociale");

        Optional<Client> existing = Optional.empty();

        if (externalClientId != null) {
            existing = clientRepository.findByBubbleExternalClientId(externalClientId);
        }

        if (existing.isEmpty() && companyName != null) {
            existing = clientRepository.findByCompanyNameIgnoreCase(companyName);
        }

        if (existing.isEmpty() && email != null) {
            existing = clientRepository.findByEmailIgnoreCase(email);
        }

        Client client = existing.orElseGet(Client::new);

        if (client.getCreatedAt() == null) {
            client.setCreatedAt(LocalDateTime.now());
        }

        client.setBubbleUserId(null);
        client.setBubbleExternalClientId(externalClientId);
        client.setClientSource(SOURCE_CLIENT_EXTERNE);

        client.setEmail(email);
        client.setFullName(getString(externalClient, "full_name"));
        client.setCompanyName(companyName);
        client.setPhone(getString(externalClient, "phone"));
        client.setAddress(getString(externalClient, "Adresse"));
        client.setUpdatedAt(LocalDateTime.now());

        return clientRepository.save(client);
    }

    private String extractEmail(Map<String, Object> bubbleUser) {
        String emailText = getString(bubbleUser, "email_text");

        if (emailText != null && !emailText.isBlank()) {
            return emailText;
        }

        Object authenticationObject = bubbleUser.get("authentication");

        if (authenticationObject instanceof Map) {
            Map<String, Object> authentication = (Map<String, Object>) authenticationObject;
            Object emailObject = authentication.get("email");

            if (emailObject instanceof Map) {
                Map<String, Object> emailMap = (Map<String, Object>) emailObject;
                return getString(emailMap, "email");
            }
        }

        return null;
    }

    private String extractAddress(Map<String, Object> bubbleUser) {
        Object addressObject = bubbleUser.get("Adresse");

        if (addressObject instanceof Map) {
            Map<String, Object> addressMap = (Map<String, Object>) addressObject;
            return getString(addressMap, "address");
        }

        return null;
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return null;
    }

    private String firstNotBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }

        if (second != null && !second.isBlank()) {
            return second;
        }

        return null;
    }

    public Client searchExternalClient(String query) {

        Map<String, Object> externalClient = bubbleApiService.searchBubbleExternalClientByEmail(query);

        if (externalClient != null) {
            return upsertFromBubbleExternalClient(externalClient);
        }

        externalClient = bubbleApiService.searchBubbleExternalClientByCompanyName(query);

        if (externalClient != null) {
            return upsertFromBubbleExternalClient(externalClient);
        }

        return null;

    }

}