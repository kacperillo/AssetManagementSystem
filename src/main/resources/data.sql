-- Initial data for AssetManagement System
-- This file contains sample data for testing purposes

-- Insert sample admin user
-- Password: admin123 (BCrypt hash)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until)
VALUES ('Administrator', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN', '2023-01-01', NULL)
ON DUPLICATE KEY UPDATE full_name=full_name;

-- Insert sample employee user
-- Password: employee123 (BCrypt hash)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until)
VALUES ('Jan Kowalski', 'jan.kowalski@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', '2023-06-15', NULL)
ON DUPLICATE KEY UPDATE full_name=full_name;

-- Note: To generate BCrypt hashes for new passwords, use an online BCrypt generator
-- or the following code in your Spring Boot application:
-- BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
-- String hashedPassword = encoder.encode("yourPassword");
