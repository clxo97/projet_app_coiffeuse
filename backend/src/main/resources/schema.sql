CREATE TABLE IF NOT EXISTS coiffeuses (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL,
    nom_salon VARCHAR(160),
    email VARCHAR(180) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    subscription_status VARCHAR(30) NOT NULL DEFAULT 'TRIAL',
    abonnement_actif_jusqu_au TIMESTAMP NULL,
    stripe_customer_id VARCHAR(120),
    stripe_subscription_id VARCHAR(120),
    cree_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modifie_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_coiffeuses_email UNIQUE (email)
);

ALTER TABLE clients ADD COLUMN IF NOT EXISTS coiffeuse_id BIGINT NULL;
ALTER TABLE depenses ADD COLUMN IF NOT EXISTS coiffeuse_id BIGINT NULL;
