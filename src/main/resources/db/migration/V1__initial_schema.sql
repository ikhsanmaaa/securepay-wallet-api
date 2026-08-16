CREATE TABLE users (
    id UUID PRIMARY KEY,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,

    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
);

CREATE TABLE wallet (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    user_id UUID NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    status VARCHAR(255) NOT NULL,

    CONSTRAINT uk_wallet_user_id UNIQUE (user_id),

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    sender_wallet_id UUID,
    receiver_wallet_id UUID,
    reference_number VARCHAR(50) NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    fee NUMERIC(19, 2) NOT NULL,
    description TEXT,

    CONSTRAINT uk_transaction_reference_number
        UNIQUE (reference_number),

    CONSTRAINT fk_transaction_sender_wallet
        FOREIGN KEY (sender_wallet_id)
        REFERENCES wallet (id),

    CONSTRAINT fk_transaction_receiver_wallet
        FOREIGN KEY (receiver_wallet_id)
        REFERENCES wallet (id)
);

CREATE TABLE mutations (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    transaction_id UUID NOT NULL,
    balance_before NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_mutations_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallet (id),

    CONSTRAINT fk_mutations_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transaction (id)
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);