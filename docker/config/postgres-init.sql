CREATE TABLE IF NOT EXISTS statements(
    client_id serial PRIMARY KEY,
    balance_limit BIGINT NOT NULL,
    current_balance BIGINT NOT NULL
);

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
