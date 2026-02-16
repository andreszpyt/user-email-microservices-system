-- Flyway migration: create emails table
CREATE TABLE IF NOT EXISTS emails (
    email_id UUID PRIMARY KEY,
    user_id UUID,
    email_from VARCHAR(255) NOT NULL,
    email_to VARCHAR(255) NOT NULL,
    email_subject VARCHAR(255),
    email_body TEXT,
    email_status VARCHAR(50)
);

