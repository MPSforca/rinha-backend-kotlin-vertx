-- Statements table
CREATE TABLE IF NOT EXISTS statements(
    client_id serial PRIMARY KEY,
    balance_limit BIGINT NOT NULL,
    current_balance BIGINT NOT NULL
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions(
    id SERIAL PRIMARY KEY,
    client_id serial NOT NULL,
    value BIGINT NOT NULL,
    type CHAR NOT NULL,
    description VARCHAR(10) NOT NULL,
    carried_out_at timestamp NOT NULL,
    CONSTRAINT fk_client_id
        FOREIGN KEY (client_id)
        REFERENCES statements(client_id)
        ON DELETE CASCADE
  );

CREATE INDEX transactions_carried_out_index ON transactions ( client_id, carried_out_at desc );

-- Save Transaction Stored Procedure
CREATE OR REPLACE PROCEDURE save_transaction(
	t_client_id bigint,
	new_client_balance bigint,
	t_value bigint,
	t_type varchar(1),
	t_description varchar(10),
	t_carried_out_at timestamp
)
LANGUAGE plpgsql AS
$$
BEGIN
    -- adding to transactions table
    INSERT INTO transactions(client_id, value, type, description, carried_out_at)
    VALUES (t_client_id, t_value, t_type, t_description, t_carried_out_at);

    -- updating client balance
    UPDATE statements
    SET current_balance = new_client_balance
    WHERE client_id = t_client_id;
END;
$$;

-- Insert initial data
DO $$
BEGIN
INSERT INTO statements(client_id, balance_limit, current_balance)
VALUES
  (1, 100000, 0),
  (2, 80000, 0),
  (3, 1000000, 0),
  (4, 10000000, 0),
  (5, 500000, 0);
END;
$$
