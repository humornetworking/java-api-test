-- ============================================================
--  Users API — Script de creación de base de datos
--  Compatible con: H2 (desarrollo) / PostgreSQL (producción)
-- ============================================================

-- ------------------------------------------------------------
--  Tabla: users
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          UUID          NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    token       VARCHAR(2048) NOT NULL,
    created     TIMESTAMP     NOT NULL,
    modified    TIMESTAMP     NOT NULL,
    last_login  TIMESTAMP     NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

-- ------------------------------------------------------------
--  Tabla: phones
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS phones (
    id          BIGINT        NOT NULL,
    number      VARCHAR(20),
    citycode    VARCHAR(10),
    contrycode  VARCHAR(10),
    user_id     UUID          NOT NULL,

    CONSTRAINT pk_phones      PRIMARY KEY (id),
    CONSTRAINT fk_phones_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- Secuencia para phones.id (H2 / PostgreSQL)
CREATE SEQUENCE IF NOT EXISTS phones_seq START WITH 1 INCREMENT BY 1;

-- ------------------------------------------------------------
--  Índices
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_users_email     ON users  (email);
CREATE INDEX IF NOT EXISTS idx_users_is_active ON users  (is_active);
CREATE INDEX IF NOT EXISTS idx_phones_user_id  ON phones (user_id);
