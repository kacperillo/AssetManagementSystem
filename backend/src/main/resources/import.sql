-- Initial data for AssetManagement System - E2E Testing Profile
-- This file is automatically loaded by Hibernate after schema creation

-- Insert sample admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until) VALUES ('Administrator', 'admin@example.com', '$2y$10$ceqHIK6g4SR2nsTS8wHRcOj5rfJF/I4dIfT4mbeEklTfFkR16QRSm', 'ADMIN', '2023-01-01', NULL);

-- Insert sample employee user
-- Password: password123 (BCrypt encoded)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until) VALUES ('Jan Kowalski', 'jan.kowalski@example.com', '$2y$10$BLGzSIgcbY2f63yA.9A.IO3zQsG/kA/vj0wtHU7fdjVwYY.VsWFY6', 'EMPLOYEE', '2023-06-15', NULL);

-- Insert test assets
INSERT INTO assets (asset_type, vendor, model, series_number, is_active) VALUES ('LAPTOP', 'Dell', 'XPS 15', 'SN-LAPTOP-001', true);
INSERT INTO assets (asset_type, vendor, model, series_number, is_active) VALUES ('SMARTPHONE', 'Apple', 'iPhone 14', 'SN-PHONE-001', true);
INSERT INTO assets (asset_type, vendor, model, series_number, is_active) VALUES ('TABLET', 'Samsung', 'Galaxy Tab S9', 'SN-TABLET-001', true);
INSERT INTO assets (asset_type, vendor, model, series_number, is_active) VALUES ('HEADPHONES', 'Sony', 'WH-1000XM5', 'SN-HEADPHONES-001', false);

-- Insert test assignment (employee Jan Kowalski has laptop assigned)
INSERT INTO assignments (asset_id, employee_id, assigned_from, assigned_until) VALUES (1, 2, '2024-01-01', NULL);

-- Test credentials:
-- admin@example.com / admin123
-- jan.kowalski@example.com / password123
