-- Database Schema for Authentication Shred
-- PostgreSQL Database Schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Accounts table (owned by Account Management Shred, included for reference)
CREATE TABLE IF NOT EXISTS accounts (
    account_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Security Settings table
CREATE TABLE IF NOT EXISTS security_settings (
    security_settings_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID UNIQUE NOT NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT false,
    mfa_method VARCHAR(10),
    login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    password_changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_security_settings_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_mfa_method CHECK (mfa_method IS NULL OR mfa_method IN ('SMS', 'EMAIL', 'TOTP')),
    CONSTRAINT chk_login_attempts CHECK (login_attempts >= 0)
);

-- Authentication Sessions table
CREATE TABLE IF NOT EXISTS authentication_sessions (
    session_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    access_token_hash VARCHAR(255) NOT NULL,
    refresh_token_hash VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_sessions_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_expires_at CHECK (expires_at > created_at)
);

-- MFA Challenges table
CREATE TABLE IF NOT EXISTS mfa_challenges (
    challenge_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    challenge_code_hash VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mfa_challenges_account FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    CONSTRAINT chk_mfa_challenge_method CHECK (method IN ('SMS', 'EMAIL', 'TOTP')),
    CONSTRAINT chk_challenge_expires_at CHECK (expires_at > created_at)
);

-- Indexes for performance optimization

-- Security Settings indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_security_settings_account_id ON security_settings(account_id);
CREATE INDEX IF NOT EXISTS idx_security_settings_locked_until ON security_settings(locked_until) WHERE locked_until IS NOT NULL;

-- Authentication Sessions indexes
CREATE INDEX IF NOT EXISTS idx_auth_sessions_account_id ON authentication_sessions(account_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_auth_sessions_refresh_token ON authentication_sessions(refresh_token_hash);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_access_token ON authentication_sessions(access_token_hash);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_expires_at ON authentication_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_auth_sessions_active ON authentication_sessions(account_id, is_revoked, expires_at) WHERE is_revoked = false;

-- MFA Challenges indexes
CREATE INDEX IF NOT EXISTS idx_mfa_challenges_account_id ON mfa_challenges(account_id);
CREATE INDEX IF NOT EXISTS idx_mfa_challenges_expires_at ON mfa_challenges(expires_at);
CREATE INDEX IF NOT EXISTS idx_mfa_challenges_active ON mfa_challenges(account_id, is_used, expires_at) WHERE is_used = false;

-- Accounts table indexes (for reference)
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_username ON accounts(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_email ON accounts(email);
CREATE INDEX IF NOT EXISTS idx_accounts_status ON accounts(account_status);

-- Triggers for automatic updated_at timestamp updates
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to all tables
CREATE TRIGGER update_accounts_updated_at BEFORE UPDATE ON accounts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_security_settings_updated_at BEFORE UPDATE ON security_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_auth_sessions_updated_at BEFORE UPDATE ON authentication_sessions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_mfa_challenges_updated_at BEFORE UPDATE ON mfa_challenges FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Initial test data for development
-- BCrypt hash for 'password' with cost=12: $2a$12$6.FuF3MQBqYh4Vc/6FKQEOXqYMEJN2XjrAGm7oaL5OuWH.8FxELvq
INSERT INTO accounts (account_id, username, password_hash, email, account_status, created_at, last_login_at)
VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'testuser1', '$2a$12$6.FuF3MQBqYh4Vc/6FKQEOXqYMEJN2XjrAGm7oaL5OuWH.8FxELvq', 'test1@example.com', 'ACTIVE', CURRENT_TIMESTAMP, NULL),
    ('550e8400-e29b-41d4-a716-446655440002', 'testuser2', '$2a$12$6.FuF3MQBqYh4Vc/6FKQEOXqYMEJN2XjrAGm7oaL5OuWH.8FxELvq', 'test2@example.com', 'ACTIVE', CURRENT_TIMESTAMP, NULL),
    ('550e8400-e29b-41d4-a716-446655440003', 'lockeduser', '$2a$12$6.FuF3MQBqYh4Vc/6FKQEOXqYMEJN2XjrAGm7oaL5OuWH.8FxELvq', 'locked@example.com', 'SUSPENDED', CURRENT_TIMESTAMP, NULL)
ON CONFLICT (account_id) DO NOTHING;

-- Initial security settings for test users
INSERT INTO security_settings (security_settings_id, account_id, mfa_enabled, mfa_method, login_attempts, locked_until, password_changed_at)
VALUES 
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', false, NULL, 0, NULL, CURRENT_TIMESTAMP),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', true, 'EMAIL', 0, NULL, CURRENT_TIMESTAMP),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', false, NULL, 5, CURRENT_TIMESTAMP + INTERVAL '15 minutes', CURRENT_TIMESTAMP)
ON CONFLICT (account_id) DO NOTHING;

-- Comments for documentation
COMMENT ON TABLE accounts IS 'User accounts table - owned by Account Management Shred';
COMMENT ON TABLE security_settings IS 'Security configuration and lockout management for user accounts';
COMMENT ON TABLE authentication_sessions IS 'Active authentication sessions with JWT token management';
COMMENT ON TABLE mfa_challenges IS 'Multi-factor authentication challenges and verification codes';

COMMENT ON COLUMN security_settings.login_attempts IS 'Counter for consecutive failed login attempts since last successful login';
COMMENT ON COLUMN security_settings.locked_until IS 'Timestamp until which the account is locked - NULL means not locked';
COMMENT ON COLUMN authentication_sessions.access_token_hash IS 'SHA-256 hash of the JWT access token for revocation checking';
COMMENT ON COLUMN authentication_sessions.refresh_token_hash IS 'SHA-256 hash of the refresh token - must be unique';
COMMENT ON COLUMN mfa_challenges.challenge_code_hash IS 'BCrypt hash of the MFA verification code';
COMMENT ON COLUMN mfa_challenges.is_used IS 'Flag indicating if this challenge has been successfully verified';
