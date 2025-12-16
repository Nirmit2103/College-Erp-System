-- Auth Database Seed Data
-- Sample users with bcrypt hashed passwords
-- Default passwords: admin123, inst123, stu123

USE univ_auth_db;

-- Clear existing data (optional, for clean seed)
-- DELETE FROM auth_password_history;
-- DELETE FROM auth_login_attempts;
-- DELETE FROM auth_users;

-- Admin user
INSERT INTO auth_users (username, role, password_hash, active) VALUES
('admin1', 'ADMIN', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqJ5q5q5qO', TRUE);
-- Password: admin123 (bcrypt hash)

-- Instructor user
INSERT INTO auth_users (username, role, password_hash, active) VALUES
('inst1', 'INSTRUCTOR', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqJ5q5q5qO', TRUE);
-- Password: inst123 (bcrypt hash)

-- Student users
INSERT INTO auth_users (username, role, password_hash, active) VALUES
('stu1', 'STUDENT', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqJ5q5q5qO', TRUE),
('stu2', 'STUDENT', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYqJ5q5q5qO', TRUE);
-- Password: stu123 (bcrypt hash)

-- Note: The above hashes are placeholders. In production, use actual bcrypt hashes.
-- To generate proper hashes, use the BCryptPasswordHasher in the application.

