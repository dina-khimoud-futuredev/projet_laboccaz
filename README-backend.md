# devis — Documentation technique backend

API REST Spring Boot servant de passerelle entre le front-office React et la base de données **Bubble** (no-code). Toutes les données métier (devis, articles, clients) vivent dans Bubble ; le backend les expose via une API unifiée et maintient une réplique locale des clients dans PostgreSQL / H2.

---

## Table des matières

1. [Stack technique](#1-stack-technique)
2. [Architecture du projet](#2-architecture-du-projet)
3. [Installation & lancement](#3-installation--lancement)
4. [Configuration](#4-configuration)
5. [Modèle de données](#5-modèle-de-données)
6. [API REST — référence complète](#6-api-rest--référence-complète)
7. [Couche service](#7-couche-service)
8. [Intégration Bubble](#8-intégration-bubble)
9. [Flux métier clés](#9-flux-métier-clés)
10. [Points d'amélioration connus](#10-points-damélioration-connus)

---

## 1. Stack technique

| Composant | Version | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | 3.5.x | Framework applicatif |
| Spring Data JPA | — | ORM / accès base de données |
| Spring Web (RestTemplate) | — | Appels HTTP vers Bubble |
| Lombok | — | Génération de getters/setters |
| PostgreSQL | — | Base de données de production |
| H2 (in-memory) | — | Base de données de développement |
| Gradle | 8.14.5 | Build tool |
| JUnit 5 | — | Tests |

> Le profil `local` est activé par défaut (`spring.profiles.active=local`). En local, H2 in-memory est utilisé ; en production, PostgreSQL prend le relais via `application-prod.properties` (à créer).

---

## 2. Architecture du projet

```
src/main/java/com/laboccaz/devis/
├── DevisApplication.java               # Point d'entrée Spring Boot
│
├── controller/
│   ├── AuthController.java             # POST /api/auth/login
│   ├── QuoteRequestController.java     # CRUD /api/quotes
│   ├── BubbleController.java           # Proxy /api/bubble/*
│   └── ClientController.java           # /api/clients
│
├── service/
│   ├── BubbleApiService.java           # Toutes les requêtes HTTP vers Bubble
│   ├── QuoteRequestService.java        # Orchestration création/modification de devis
│   └── ClientService.java              # Synchronisation & upsert des clients locaux
│
├── entity/
│   ├── QuoteRequest.java               # Entité JPA (table quote_requests)
│   ├── Client.java                     # Entité JPA (table clients)
│   └── QuoteStatus.java                # Enum des statuts
│
├── dto/
│   └── QuoteRequestCreateDto.java      # DTO entrée pour création/modification
│
└── repository/
    ├── QuoteRequestRepository.java     # Spring Data JPA
    └── ClientRepository.java          # Spring Data JPA

src/main/resources/
├── application.properties              # Config principale (H2, Bubble URL)
└── application-local.properties        # Token Bubble (ne pas committer en prod)
```

> **Architecture en couches** : Controller → Service → BubbleApiService / Repository. Les controllers ne contiennent aucune logique métier.

---

## 3. Installation & lancement

### Prérequis

- Java 17+
- Gradle (le wrapper `./gradlew` est inclus)
- Token API Bubble (voir section Configuration)

### Commandes

```bash
# Lancer le serveur de développement (profil local, H2)
./gradlew bootRun

# Build du JAR
./gradlew build

# Lancer le JAR buildé
java -jar build/libs/devis-0.0.1-SNAPSHOT.jar

# Tests
./gradlew test
```

Le serveur démarre sur **`http://localhost:8080`**.

La console H2 est accessible à **`http://localhost:8080/h2-console`**  
(URL JDBC : `jdbc:h2:mem:laboccaz_db`, login : `sa`, pas de mot de passe)

---

## 4. Configuration

### `application.properties` (config principale)

```properties
# Base de données — H2 in-memory par défaut
spring.datasource.url=jdbc:h2:mem:laboccaz_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Console H2 (dev uniquement)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Profil actif
spring.profiles.active=local

# Bubble API
bubble.api.base-url=https://app.laboccaz.com/version-63d6t/api/1.1/obj
bubble.api.token=${BUBBLE_API_TOKEN:}
```

### `application-local.properties` (token dev — à ne PAS committer en prod)

```properties
bubble.api.token=<TOKEN_ICI>
```

> ⚠️ Ce fichier contient un token d'API en clair. Il est listé dans `.gitignore` mais il était présent dans l'archive. En production, utiliser une variable d'environnement `BUBBLE_API_TOKEN` ou un secret manager.

### Pour PostgreSQL (production)

Créer un fichier `application-prod.properties` :

```properties
spring.datasource.url=jdbc:postgresql://HOST:5432/laboccaz_db
spring.datasource.username=USER
spring.datasource.password=PASSWORD
spring.datasource.driverClassName=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=false
```

Et lancer avec :
```bash
java -jar devis.jar --spring.profiles.active=prod
```

---

## 5. Modèle de données

### `QuoteRequest` — table `quote_requests`

Réplique locale d'une demande de devis (source de vérité : Bubble `Demande_devis`).

| Colonne | Type Java | Description |
|---|---|---|
| `id` | `Long` | Clé primaire auto-incrémentée |
| `clientName` | `String` | Nom complet du client |
| `clientEmail` | `String` | Email du client |
| `clientPhone` | `String` | Téléphone |
| `companyName` | `String` | Raison sociale |
| `productReference` | `String` | Référence article |
| `productName` | `String` | Nom de l'article |
| `bubbleArticleId` | `String` | ID de l'article dans Bubble |
| `unitPriceHt` | `Double` | Prix unitaire HT |
| `quantity` | `Integer` | Quantité |
| `requestDescription` | `TEXT` | Description libre |
| `status` | `QuoteStatus` (enum) | Statut de la demande |
| `source` | `String` | Origine (`MANUAL`, etc.) |
| `createdAt` | `LocalDateTime` | Date de création |
| `updatedAt` | `LocalDateTime` | Date de dernière modification |

> ⚠️ Cette entité locale n'est actuellement **pas utilisée pour les lectures/écritures** : toutes les opérations CRUD passent directement par Bubble via `BubbleApiService`. Elle semble prévue pour une future persistance locale ou synchronisation.

### `Client` — table `clients`

Réplique locale des clients, synchronisée depuis Bubble lors de la création/modification d'un devis.

| Colonne | Type Java | Description |
|---|---|---|
| `id` | `Long` | Clé primaire auto-incrémentée |
| `bubbleUserId` | `String` (unique) | ID Bubble de type `User` |
| `bubbleExternalClientId` | `String` | ID Bubble de type `Client_externe` |
| `clientSource` | `String` | `USER` ou `CLIENT_EXTERNE` |
| `email` | `String` | Email |
| `firstName` / `lastName` | `String` | Prénom / Nom |
| `fullName` | `String` | Nom complet |
| `companyName` | `String` | Raison sociale |
| `phone` | `String` | Téléphone |
| `address` | `TEXT` | Adresse |
| `role` | `String` | Rôle Bubble |
| `sellerType` | `String` | Type vendeur |
| `accountStatus` | `String` | Statut du compte Bubble |
| `profileCompleted` | `Boolean` | Profil Bubble complété |
| `createdAt` / `updatedAt` | `LocalDateTime` | Horodatages |

### `QuoteStatus` — enum

```java
RECEIVED, IN_REVIEW, QUOTE_GENERATED, QUOTE_SENT, ACCEPTED, REJECTED
```

> Note : `CANCELLED` apparaît dans le front mais **n'est pas dans l'enum** côté backend. À ajouter si le statut est utilisé.

---

## 6. API REST — référence complète

Toutes les routes ont `@CrossOrigin(origins = "http://localhost:5173")`.

> ⚠️ Le CORS est hardcodé pour le dev local. À rendre configurable pour la production.

---

### Auth — `/api/auth`

#### `POST /api/auth/login`

Authentification d'un administrateur. Le mot de passe est comparé en **texte clair** au champ `password_hash` de Bubble.

**Corps de la requête :**
```json
{
  "email": "admin@laboccaz.com",
  "password": "mon_mot_de_passe"
}
```

**Réponse succès :**
```json
{ "success": true, "firstName": "Sandrine" }
```

**Réponse échec :**
```json
{ "success": false, "message": "Mot de passe incorrect" }
```

**Flux interne :** requête Bubble `GET /Admin_Devis?constraints=[{"key":"email","constraint_type":"equals","value":"..."}]`

> ⚠️ Sécurité critique : le mot de passe est comparé en clair (`password.equals(passwordHash)`). Bubble stocke vraisemblablement un hash bcrypt dans ce champ — la comparaison en clair ne fonctionnera jamais correctement. À corriger avec BCrypt ou à déléguer à Bubble.

---

### Devis — `/api/quotes`

#### `GET /api/quotes`
Retourne toutes les demandes depuis Bubble `Demande_devis`.

**Réponse :** corps JSON Bubble brut (`{ "response": { "results": [...] } }`)

---

#### `GET /api/quotes/{id}`
Retourne une demande par son ID Bubble.

**Réponse :** `{ "response": { ...champs de la demande... } }`

---

#### `POST /api/quotes`
Crée une nouvelle demande. Déclenche aussi un **upsert du client** en local.

**Corps :**
```json
{
  "clientName": "Jean Dupont",
  "clientEmail": "jean@example.com",
  "clientPhone": "06 00 00 00 00",
  "companyName": "Lab Martin",
  "bubbleArticleId": "1234abc",
  "productName": "Centrifugeuse Z200",
  "productReference": "Z200-REF",
  "unitPriceHt": 1200.0,
  "quantity": 2,
  "requestDescription": "Besoin urgent",
  "source": "MANUAL"
}
```

Champs ajoutés automatiquement avant envoi à Bubble :
- `status` → `"RECEIVED"`
- `crm_sync_status` → `"PENDING"`
- `created_from_backend` → `true`

---

#### `PUT /api/quotes/{id}`
Modifie une demande existante. Déclenche aussi un upsert du client.

Même corps que `POST`, sans `source`.

---

#### `PATCH /api/quotes/{id}/status?status=IN_REVIEW`
Met à jour uniquement le statut d'une demande.

**Fonctionnement :** récupère la demande complète dans Bubble, remplace le champ `status`, supprime les champs système (`_id`, `Created Date`, `Modified Date`, `Created By`), puis renvoie l'objet complet en `PUT`.

---

#### `DELETE /api/quotes/{id}`
Supprime une demande dans Bubble.

---

### Bubble (proxy) — `/api/bubble`

#### `GET /api/bubble/articles`
Retourne tous les articles depuis Bubble `Article`.

#### `GET /api/bubble/articles/{id}`
Retourne un article par son ID Bubble.

#### `GET /api/bubble/articles/slug/{slug}`
Recherche un article par son slug (contrainte Bubble `Slug equals`).

#### `GET /api/bubble/clients/search?email=&companyName=`
Recherche un utilisateur Bubble (`User`) par email ou raison sociale.  
Retourne le corps Bubble brut (liste).

#### `GET /api/bubble/proxy-image?url=https://...`
Proxy CORS pour les images hébergées sur Bubble.  
Récupère l'image distante et la retransmet avec le bon `Content-Type`.

---

### Clients locaux — `/api/clients`

#### `GET /api/clients`
Retourne tous les clients de la base locale.

#### `POST /api/clients/sync/bubble/{bubbleUserId}`
Synchronise un client Bubble `User` en base locale (upsert).

#### `POST /api/clients/sync/bubble/external/{externalClientId}`
Synchronise un client Bubble `Client_externe` en base locale (upsert).

#### `GET /api/clients/external/search?query=xxx`
Recherche un client externe dans Bubble (par email, puis par raison sociale) et le synchronise en local.  
Retourne l'entité `Client` locale.

---

## 7. Couche service

### `BubbleApiService`

Point d'entrée unique pour tous les appels HTTP vers Bubble. Utilise `RestTemplate` avec header `Authorization: Bearer <token>`.

| Méthode | Cible Bubble | Description |
|---|---|---|
| `getQuoteRequests()` | `GET /Demande_devis` | Liste toutes les demandes |
| `getQuoteRequestById(id)` | `GET /Demande_devis/{id}` | Détail d'une demande |
| `createQuoteRequest(dto)` | `POST /Demande_devis` | Crée une demande |
| `updateQuoteRequest(id, dto)` | `PUT /Demande_devis/{id}` | Modifie une demande |
| `updateQuoteRequestStatus(id, status)` | `GET` puis `PUT /Demande_devis/{id}` | Met à jour le statut |
| `deleteQuoteRequest(id)` | `DELETE /Demande_devis/{id}` | Supprime une demande |
| `getArticles()` | `GET /Article` | Liste les articles |
| `getArticleById(id)` | `GET /Article/{id}` | Détail d'un article |
| `getArticleBySlug(slug)` | `GET /Article?constraints=...` | Recherche par slug |
| `getAdminByEmail(email)` | `GET /Admin_Devis?constraints=...` | Auth admin |
| `searchClient(email, companyName)` | `GET /User?constraints=...` | Recherche User Bubble |
| `getBubbleUserById(id)` | `GET /User/{id}` | User Bubble par ID |
| `searchBubbleUserByEmailText(email)` | `GET /User?constraints=...` | User par email |
| `searchBubbleUserByCompanyName(name)` | `GET /User?constraints=...` | User par raison sociale (essaie les deux champs) |
| `searchBubbleExternalClientByEmail(email)` | `GET /Client_externe?constraints=...` | Client externe par email |
| `searchBubbleExternalClientByCompanyName(name)` | `GET /Client_externe?constraints=...` | Client externe par raison sociale |
| `getBubbleExternalClientById(id)` | `GET /Client_externe/{id}` | Client externe par ID |
| `createBubbleExternalClient(...)` | `POST /Client_externe` | Crée un client externe dans Bubble |

---

### `QuoteRequestService`

Orchestration simple : délègue à `BubbleApiService` pour les opérations Bubble, et déclenche `ClientService.upsertFromQuoteDto()` lors de chaque création ou modification pour maintenir la base locale synchronisée.

---

### `ClientService`

Gère la synchronisation bidirectionnelle des clients entre Bubble et la base locale.

**Logique d'upsert** (même pour `User` et `Client_externe`) :
1. Cherche par ID Bubble
2. Si non trouvé → cherche par raison sociale
3. Si non trouvé → cherche par email
4. Si toujours non trouvé → crée une nouvelle entité

**`upsertFromQuoteDto(dto)`** — appelé à chaque POST/PUT sur `/api/quotes` :
1. Cherche si le client existe déjà dans Bubble `Client_externe` (par email, puis raison sociale)
2. Si trouvé → upsert en local
3. Si non trouvé → crée le client dans Bubble `Client_externe`, puis upsert en local

**Deux sources de clients :**

| Source | Champ identifiant | Champ `clientSource` |
|---|---|---|
| Bubble `User` | `bubbleUserId` | `USER` |
| Bubble `Client_externe` | `bubbleExternalClientId` | `CLIENT_EXTERNE` |

---

## 8. Intégration Bubble

L'application est un **proxy / BFF (Backend for Frontend)** devant Bubble.

### URL de base Bubble
```
https://app.laboccaz.com/version-63d6t/api/1.1/obj
```

### Objets Bubble utilisés

| Objet Bubble | Usage |
|---|---|
| `Demande_devis` | CRUD complet des demandes de devis |
| `Article` | Lecture du catalogue produits |
| `User` | Recherche/lecture des clients Laboccaz |
| `Client_externe` | Recherche/création/lecture des clients externes |
| `Admin_Devis` | Authentification des administrateurs |

### Format des contraintes Bubble
```json
[{"key": "email_text", "constraint_type": "equals", "value": "test@example.com"}]
```

Passées en query param URL-encodé : `?constraints=[...]`

### Particularité du mapping `User`

L'email d'un `User` Bubble peut être dans deux emplacements :
- `email_text` (champ texte direct)
- `authentication.email.email` (objet imbriqué)

`ClientService.extractEmail()` gère les deux cas.

---

## 9. Flux métier clés

### Création d'une demande (`POST /api/quotes`)

```
Front → POST /api/quotes (QuoteRequestCreateDto)
  └─ QuoteRequestService.createQuoteRequest(dto)
       ├─ ClientService.upsertFromQuoteDto(dto)
       │    ├─ Bubble: searchBubbleExternalClientByEmail(email)  → trouvé ? upsert local
       │    ├─ Bubble: searchBubbleExternalClientByCompanyName(name) → trouvé ? upsert local
       │    └─ sinon: Bubble POST /Client_externe → upsert local
       └─ BubbleApiService.createQuoteRequest(dto)
            └─ Bubble POST /Demande_devis { status: "RECEIVED", ... }
```

### Mise à jour du statut (`PATCH /api/quotes/{id}/status`)

```
Front → PATCH /api/quotes/{id}/status?status=IN_REVIEW
  └─ BubbleApiService.updateQuoteRequestStatus(id, status)
       ├─ Bubble GET /Demande_devis/{id}   ← récupère l'objet complet
       ├─ responseMap.put("status", newStatus)
       ├─ responseMap.remove("_id", "Created Date", ...)
       └─ Bubble PUT /Demande_devis/{id}   ← renvoie tout l'objet modifié
```

### Génération PDF (côté front uniquement)

Le backend sert ici de **proxy d'image** via `GET /api/bubble/proxy-image?url=` pour contourner les restrictions CORS des images Bubble. La génération PDF elle-même est entièrement côté front (html2canvas + jsPDF).

---

## 10. Points d'amélioration connus

### Sécurité

**Critique — authentification en clair :**  
`AuthController` compare `password.equals(passwordHash)`. Si Bubble stocke un hash bcrypt, cette comparaison sera toujours `false`. À corriger avec `BCryptPasswordEncoder.matches()` ou en déléguant l'auth à un endpoint Bubble dédié.

**Token Bubble dans `application-local.properties` :**  
Présent en clair dans le fichier commité. À remplacer par une variable d'environnement (`BUBBLE_API_TOKEN`) ou un secret manager (Vault, AWS Secrets Manager).

**CORS hardcodé :**  
`@CrossOrigin(origins = "http://localhost:5173")` sur tous les controllers. À centraliser dans une configuration Spring (`WebMvcConfigurer`) avec les origines en propriété.

**Pas d'authentification sur les endpoints :**  
Tous les endpoints (`/api/quotes`, `/api/clients`, etc.) sont accessibles sans token. À protéger avec Spring Security + JWT.

### Architecture

**`RestTemplate` instancié à chaque appel :**  
`BubbleApiService` crée une nouvelle instance de `RestTemplate` dans chaque méthode. À remplacer par un `RestTemplate` ou `WebClient` injecté en singleton (`@Bean`), avec timeout configuré.

**`QuoteRequest` entity inutilisée :**  
L'entité JPA `QuoteRequest` (et son repository) n'est jamais lue ni écrite — toutes les opérations passent par Bubble. À supprimer ou à utiliser pour une vraie persistance locale avec synchronisation asynchrone.

**Injection de `ClientRepository` dans `ClientController` :**  
Le controller injecte directement le repository en plus du service, ce qui casse l'encapsulation. La méthode `getAllClients()` devrait passer par `ClientService`.

**`CANCELLED` manquant dans l'enum :**  
Le front envoie/affiche le statut `CANCELLED` mais il n'est pas dans `QuoteStatus`. Ajouter `CANCELLED` à l'enum.

### Qualité

**Pas de gestion d'erreur HTTP :**  
Les appels `RestTemplate` ne gèrent pas les erreurs 4xx/5xx de Bubble (`HttpClientErrorException`, `HttpServerErrorException`). Une exception non catchée renvoie une réponse 500 générique au front.

**`System.out.println` en production :**  
`BubbleApiService.searchClient()` contient des `System.out.println`. À remplacer par `SLF4J` / `Logback`.

**Pas de validation des DTOs :**  
`QuoteRequestCreateDto` n'utilise pas les annotations `@NotBlank`, `@Email`, etc. de Spring Validation (la dépendance `spring-boot-starter-validation` est déclarée mais pas utilisée).

**Injection de contraintes Bubble par concaténation de chaînes :**  
```java
String constraints = "[{\"key\":\"email\",\"constraint_type\":\"equals\",\"value\":\"" + email + "\"}]";
```
Risque d'injection si `email` contient des guillemets. À construire avec un sérialiseur JSON (`ObjectMapper`).
