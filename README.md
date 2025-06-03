# eBanca - Application Bancaire en Ligne

eBanca est une application web complète permettant aux clients et aux employés de la banque de gérer différentes opérations bancaires depuis une interface web et mobile.

## Architecture

Le projet est construit avec :
- Backend : Spring Boot 3.2.3
- Frontend Client : Angular
- Frontend Admin : Thymeleaf
- Base de données : H2 (développement) / PostgreSQL (production)

## Fonctionnalités
<img width="767" alt="eBanca" src="https://github.com/user-attachments/assets/ef4c00d0-bf0d-43dc-a533-d3766fe99fde" />

### Espace Client (Angular)
- Création de compte client avec vérification d'identité
- Authentification sécurisée par JWT
- Consultation des soldes et historique des transactions
- Virements entre comptes
- Demande de prêts et crédits
- Gestion des informations personnelles
- Messagerie sécurisée avec le conseiller

### Espace Employé/Admin (Thymeleaf)
- Tableau de bord administratif
- Gestion des comptes clients
- Suivi des transactions suspectes
- Traitement des demandes de prêts
- Gestion des utilisateurs et des rôles
- Statistiques dynamiques

### Sécurité
- Authentification et autorisation par rôles
- Protection CSRF et CORS
- Journalisation des opérations
- Notifications par email

## Prérequis

- Java 17 ou supérieur
- Node.js 18 ou supérieur
- Maven 3.8 ou supérieur
- Angular CLI 16 ou supérieur

## Installation

1. Cloner le repository :
```bash
git clone https://github.com/votre-username/ebanca.git
cd ebanca
```

2. Configurer le backend :
```bash
cd backend
mvn clean install
```

3. Configurer le frontend client :
```bash
cd frontend/client
npm install
```

4. Configurer le frontend admin :
```bash
cd frontend/admin
mvn clean install
```

## Configuration

1. Backend :
- Copier `application.properties.example` vers `application.properties`
- Configurer les paramètres de base de données et email

2. Frontend Client :
- Copier `environment.example.ts` vers `environment.ts`
- Configurer l'URL de l'API

## Démarrage

1. Démarrer le backend :
```bash
cd backend
mvn spring-boot:run
```

2. Démarrer le frontend client :
```bash
cd frontend/client
ng serve
```

3. Démarrer le frontend admin :
```bash
cd frontend/admin
mvn spring-boot:run
```

## Accès

- Interface Client : http://localhost:4200
- Interface Admin : http://localhost:8080/admin
- API REST : http://localhost:8080/api
- Console H2 : http://localhost:8080/h2-console

## Tests

```bash
# Tests backend
cd backend
mvn test

# Tests frontend client
cd frontend/client
ng test
```

## Contribution

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pousser vers la branche
5. Ouvrir une Pull Request

## Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

# Cloner le repo
git clone <url-du-repo>
cd eBanca

# Lancer l'application Spring Boot
./mvnw spring-boot:run
# ou
mvn spring-boot:run

mvn javadoc:javadoc 
