-- Everything stored so far is spending, so the defaults keep it that way.
ALTER TABLE categories
    ADD COLUMN kind TEXT NOT NULL DEFAULT 'SPENDING'
        CONSTRAINT categories_kind_check CHECK (kind IN ('SPENDING', 'INCOME'));

ALTER TABLE transactions
    ADD COLUMN direction TEXT NOT NULL DEFAULT 'OUT'
        CONSTRAINT transactions_direction_check CHECK (direction IN ('OUT', 'IN'));

-- Existing users get the income categories that new users start with.
INSERT INTO categories (user_id, name, kind)
SELECT users.id, income.name, 'INCOME'
FROM users
CROSS JOIN (VALUES ('Other income'), ('Salary')) AS income (name)
ON CONFLICT DO NOTHING;
