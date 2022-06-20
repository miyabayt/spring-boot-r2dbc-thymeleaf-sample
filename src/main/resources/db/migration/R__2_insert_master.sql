DELETE FROM staffs WHERE email = 'test@example.com';
INSERT INTO staffs(id, first_name, last_name, email, password, tel, created_by, created_at) VALUES
(uuid(), 'john', 'doe', 'test@example.com', '$2a$06$hY5MzfruCds1t5uFLzrlBuw3HcrEGeysr9xJE4Cml5xEOVf425pmK', '09011112222', 'none', NOW());

DELETE FROM users WHERE email = 'test@example.com';
INSERT INTO users(id, first_name, last_name, email, password, tel, address, created_by, created_at) VALUES
(uuid(), 'john', 'doe', 'test@example.com', '$2a$06$hY5MzfruCds1t5uFLzrlBuw3HcrEGeysr9xJE4Cml5xEOVf425pmK', '09011112222', 'tokyo, chuo-ku 1-2-3', 'none', NOW());

DELETE FROM mail_templates WHERE created_by = 'none';
INSERT INTO mail_templates (id, category_code, template_code, subject, template_body, created_by, created_at) VALUES
(uuid(), NULL, 'passwordReset', 'パスワードリセット完了のお願い', CONCAT('[[$', '{staff.firstName}]]さん\r\n\r\n下記のリンクを開いてパスワードをリセットしてください。\r\n[[$', '{url}]]'), 'none', NOW());
