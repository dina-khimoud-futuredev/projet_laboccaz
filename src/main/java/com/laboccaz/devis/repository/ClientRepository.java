package com.laboccaz.devis.repository;

import com.laboccaz.devis.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByBubbleUserId(String bubbleUserId);

    Optional<Client> findByEmailIgnoreCase(String email);

    Optional<Client> findByCompanyNameIgnoreCase(String companyName);
    Optional<Client> findByBubbleExternalClientId(String bubbleExternalClientId);
}