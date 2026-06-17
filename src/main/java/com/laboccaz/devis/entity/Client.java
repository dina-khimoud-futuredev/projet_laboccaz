package com.laboccaz.devis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Getter
@Setter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String bubbleUserId;

    private String email;

    private String firstName;

    private String lastName;

    private String fullName;

    private String companyName;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    private String role;

    private String sellerType;

    private String accountStatus;

    private Boolean profileCompleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String bubbleExternalClientId;

    private String clientSource;



}