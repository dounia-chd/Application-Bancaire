-- Suppression des tables existantes
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Création des tables
CREATE TABLE roles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_roles (
    user_id INTEGER,
    role_id INTEGER,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'EUR',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    card_number VARCHAR(16) NOT NULL UNIQUE,
    card_type VARCHAR(20) NOT NULL,
    expiry_date DATE NOT NULL,
    cvv VARCHAR(3) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'COMPLETED',
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Insertion des rôles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Insertion des utilisateurs (mot de passe: password123)
INSERT INTO users (username, password, email, first_name, last_name, phone_number) VALUES
('admin', '$2a$10$rDkPvvAFV6GgJjXpYWxqUOQZx5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5', 'admin@ebanca.com', 'Admin', 'User', '+33123456789'),
('user1', '$2a$10$rDkPvvAFV6GgJjXpYWxqUOQZx5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5', 'user1@example.com', 'John', 'Doe', '+33612345678'),
('user2', '$2a$10$rDkPvvAFV6GgJjXpYWxqUOQZx5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5', 'user2@example.com', 'Jane', 'Smith', '+33623456789');

-- Attribution des rôles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 2), -- admin -> ROLE_ADMIN
(2, 1), -- user1 -> ROLE_USER
(3, 1); -- user2 -> ROLE_USER

-- Création des comptes
INSERT INTO accounts (user_id, account_number, account_type, balance, currency) VALUES
(2, 'FR7630001007941234567890185', 'CHECKING', 5000.00, 'EUR'),
(2, 'FR7630001007941234567890186', 'SAVINGS', 10000.00, 'EUR'),
(3, 'FR7630001007941234567890187', 'CHECKING', 3000.00, 'EUR'),
(3, 'FR7630001007941234567890188', 'SAVINGS', 7500.00, 'EUR');

-- Création des cartes
INSERT INTO cards (account_id, card_number, card_type, expiry_date, cvv, status) VALUES
(1, '4532123456781234', 'VISA', '2025-12-31', '123', 'ACTIVE'),
(1, '5578123456781234', 'MASTERCARD', '2025-12-31', '456', 'ACTIVE'),
(3, '4532123456785678', 'VISA', '2025-12-31', '789', 'ACTIVE');

-- Création des transactions
INSERT INTO transactions (account_id, transaction_type, amount, description) VALUES
(1, 'DEPOSIT', 1000.00, 'Dépôt initial'),
(1, 'WITHDRAWAL', -500.00, 'Retrait ATM'),
(1, 'TRANSFER', -200.00, 'Virement vers compte épargne'),
(2, 'TRANSFER', 200.00, 'Virement depuis compte courant'),
(3, 'DEPOSIT', 500.00, 'Dépôt initial'),
(3, 'WITHDRAWAL', -100.00, 'Retrait ATM'),
(4, 'DEPOSIT', 1000.00, 'Dépôt initial'); 