DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, email, phone, password, role, created_at, updated_at)
VALUES ('admin', 'admin@greenfood.com', NULL, '$2a$10$uwGVjWNn8TrqPG3t6/Ky9eZRzN.g58MhaHulCsCK2XCVTrhYLPHZG', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (username, email, phone, password, role, created_at, updated_at)
VALUES ('manager', 'manager@greenfood.com', NULL, '$2a$10$uwGVjWNn8TrqPG3t6/Ky9eZRzN.g58MhaHulCsCK2XCVTrhYLPHZG', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
