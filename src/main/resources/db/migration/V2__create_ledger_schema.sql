CREATE TABLE ledger_account (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL,
    wallet_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_ledger_account_code UNIQUE (code),
    CONSTRAINT uk_ledger_account_wallet UNIQUE (wallet_id),
    CONSTRAINT fk_ledger_account_wallet FOREIGN KEY (wallet_id) REFERENCES wallet (id)
);

CREATE TABLE ledger_entry (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    direction VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_ledger_entry_amount CHECK (amount > 0),
    CONSTRAINT fk_ledger_entry_transaction FOREIGN KEY (transaction_id) REFERENCES transaction (id),
    CONSTRAINT fk_ledger_entry_account FOREIGN KEY (account_id) REFERENCES ledger_account (id)
);

CREATE INDEX idx_ledger_entry_account ON ledger_entry (account_id, created_at);
CREATE INDEX idx_ledger_entry_transaction ON ledger_entry (transaction_id);
