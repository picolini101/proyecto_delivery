-- D-10: login with email and password. Only the BCrypt hash is stored; existing users get one when they set a
-- password (the sample users get theirs at startup).
ALTER TABLE users
    ADD COLUMN password_hash VARCHAR(100) NULL;
