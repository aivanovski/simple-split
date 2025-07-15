CREATE TABLE IF NOT EXISTS users (
    uid UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS groups (
    uid UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS group_members (
    group_uid UUID NOT NULL REFERENCES groups(uid) ON DELETE CASCADE,
    user_uid UUID NOT NULL,
    PRIMARY KEY (group_uid, user_uid) -- Composite primary key to ensure uniqueness
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
    user_uid UUID NOT NULL,
    PRIMARY KEY (expense_uid, user_uid) -- Composite primary key to ensure uniqueness
);

CREATE TABLE IF NOT EXISTS split_between (
    expense_uid UUID NOT NULL REFERENCES expenses(uid) ON DELETE CASCADE,
    group_uid UUID NOT NULL,
    user_uid UUID NOT NULL,
    PRIMARY KEY (expense_uid, user_uid) -- Composite primary key to ensure uniqueness
);