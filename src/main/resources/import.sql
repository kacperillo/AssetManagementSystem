-- Initial data for AssetManagement System - E2E Testing Profile
-- This file is automatically loaded by Hibernate after schema creation

-- Insert sample admin user
-- Password: admin123 (BCrypt encoded)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until) VALUES ('Administrator', 'admin@example.com', '$2y$10$ceqHIK6g4SR2nsTS8wHRcOj5rfJF/I4dIfT4mbeEklTfFkR16QRSm', 'ADMIN', '2023-01-01', NULL);

-- Insert sample employee user
-- Password: employee123 (BCrypt encoded)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until) VALUES ('Jan Kowalski', 'jan.kowalski@example.com', '$2y$10$BLGzSIgcbY2f63yA.9A.IO3zQsG/kA/vj0wtHU7fdjVwYY.VsWFY6', 'EMPLOYEE', '2023-06-15', NULL);

-- Test credentials:
-- admin@example.com / admin123
-- jan.kowalski@example.com / employee123
