CREATE TABLE IF NOT EXISTS users (
    uid UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS groups (
    uid UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),
    currency_iso_code VARCHAR(3) NOT NULL
);

CREATE TABLE IF NOT EXISTS group_members (
    uid UUID PRIMARY KEY,
    group_uid UUID NOT NULL REFERENCES groups(uid) ON DELETE CASCADE,
    user_uid UUID NOT NULL REFERENCES users(uid) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expenses (
    uid UUID PRIMARY KEY,
    group_uid UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    is_split_between_all BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS paid_by (
    expense_uid UUID NOT NULL REFERENCES expenses(uid) ON DELETE CASCADE,
    group_uid UUID NOT NULL,
    member_uid UUID NOT NULL,
    PRIMARY KEY (expense_uid, member_uid) -- Composite primary key to ensure uniqueness
);

CREATE TABLE IF NOT EXISTS split_between (
    expense_uid UUID NOT NULL REFERENCES expenses(uid) ON DELETE CASCADE,
    group_uid UUID NOT NULL,
    member_uid UUID NOT NULL,
    PRIMARY KEY (expense_uid, member_uid) -- Composite primary key to ensure uniqueness
);

CREATE TABLE IF NOT EXISTS currencies (
    iso_code VARCHAR(3) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(16) NOT NULL
);