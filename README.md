# Projet Laboccaz - Centralisation des demandes de devis

## Contexte

Ce projet a pour objectif de centraliser les demandes de devis entre la marketplace Laboccaz (Bubble) et un futur CRM.

L'application est développée en Spring Boot et communique avec l'API Bubble afin de créer, consulter et mettre à jour les demandes de devis.

---

## Objectifs du projet

* Centraliser les demandes de devis.
* Réduire les doubles saisies.
* Structurer les données commerciales.
* Faciliter le suivi des demandes.
* Préparer l'intégration avec un CRM.
* Automatiser les échanges entre les plateformes.

---

## Architecture

```text
Marketplace Bubble
        ↓
Spring Boot API
        ↓
Demande_devis (Bubble)
        ↓
CRM (à venir)
```

---

## Fonctionnalités développées

### Création d'une demande de devis

```http
POST /api/quotes
```

Permet de créer une demande de devis dans Bubble.

---

### Consultation de toutes les demandes

```http
GET /api/quotes
```

Permet de récupérer toutes les demandes de devis stockées dans Bubble.

---

### Consultation d'une demande

```http
GET /api/quotes/{id}
```

Permet de récupérer les informations d'une demande de devis spécifique.

---

### Mise à jour du statut

```http
PATCH /api/quotes/{id}/status
```

Permet de modifier le statut d'une demande de devis dans Bubble.

Exemple :

```http
PATCH /api/quotes/1780406639064x759129524280623700/status?status=IN_REVIEW
```

---

## Structure des données

### Demande_devis

| Champ                | Type         |
| -------------------- | ------------ |
| article              | Article      |
| article_id           | Text         |
| client_name          | Text         |
| client_email         | Text         |
| client_phone         | Text         |
| company_name         | Text         |
| product_name         | Text         |
| product_reference    | Text         |
| quantity             | Number       |
| unit_price_ht        | Number       |
| request_description  | Text         |
| source               | Text         |
| status               | Devis Status |
| crm_sync_status      | Text         |
| crm_id               | Text         |
| created_from_backend | Yes / No     |

---

## Flux métier actuel

1. Le client effectue une demande de devis.
2. Spring Boot reçoit la demande.
3. Spring Boot crée la demande dans Bubble.
4. Les demandes peuvent être consultées.
5. Les statuts peuvent être mis à jour.

---

## Technologies utilisées

* Java 17
* Spring Boot
* Gradle
* Bubble Data API
* H2 Database (développement)
* Git / GitHub

---

## Évolutions prévues

* Synchronisation avec le CRM.
* Remontée des statuts CRM vers Bubble.
* Mise en place éventuelle d'un back-office React.
* Passage à PostgreSQL.
* Sécurisation des clés API via variables d'environnement.

---

## Auteur

Projet réalisé dans le cadre du projet Laboccaz.
