package com.laboccaz.devis.controller;

import com.laboccaz.devis.entity.Client;
import com.laboccaz.devis.repository.ClientRepository;
import com.laboccaz.devis.service.ClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "http://localhost:5173")
public class ClientController {

    private final ClientService clientService;
    private final ClientRepository clientRepository;

    public ClientController(ClientService clientService, ClientRepository clientRepository) {
        this.clientService = clientService;
        this.clientRepository = clientRepository;
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @PostMapping("/sync/bubble/{bubbleUserId}")
    public Client syncClientFromBubble(@PathVariable String bubbleUserId) {
        return clientService.syncFromBubbleUserId(bubbleUserId);
    }

    @PostMapping("/sync/bubble/external/{externalClientId}")
    public Client syncExternalClientFromBubble(@PathVariable String externalClientId) {
        return clientService.syncFromBubbleExternalClientId(externalClientId);
    }

    @GetMapping("/external/search")
    public Client searchExternalClient(
            @RequestParam String query) {

        return clientService.searchExternalClient(query);
    }

}