-- Conta bloqueada pelo administrador: não entra nem renova a sessão.
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_users_role ON users (role);
