-- Insert sample admin user
-- Password: admin123 (BCrypt hash)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until)
VALUES ('Administrator', 'admin@example.com', '$2y$10$ceqHIK6g4SR2nsTS8wHRcOj5rfJF/I4dIfT4mbeEklTfFkR16QRSm', 'ADMIN', '2023-01-01', NULL)
ON DUPLICATE KEY UPDATE full_name=full_name;

-- Insert sample employee user
-- Password: password123 (BCrypt hash)
INSERT INTO employees (full_name, email, password, role, hired_from, hired_until)
VALUES ('Jan Kowalski', 'jan.kowalski@example.com', '$2y$10$BLGzSIgcbY2f63yA.9A.IO3zQsG/kA/vj0wtHU7fdjVwYY.VsWFY6', 'EMPLOYEE', '2023-06-15', NULL)
ON DUPLICATE KEY UPDATE full_name=full_name;


