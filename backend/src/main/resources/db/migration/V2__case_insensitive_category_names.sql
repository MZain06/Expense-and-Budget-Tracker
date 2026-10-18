-- A user can't have two categories whose names differ only in case, like "Food" and "food".
-- The index on lower(name) enforces that, so it replaces the case-sensitive rule from V1.
ALTER TABLE categories DROP CONSTRAINT IF EXISTS categories_user_id_name_key;

CREATE UNIQUE INDEX categories_user_id_lower_name_key ON categories (user_id, lower(name));