-- 修改 users 表，允许 email 和 phone 字段为 NULL
-- 但至少需要有一个不为 NULL（通过应用层逻辑保证）

-- 如果 email 字段当前不允许为 NULL，需要先修改
ALTER TABLE users MODIFY COLUMN email VARCHAR(255) NULL COMMENT '邮箱地址';

-- 如果 phone 字段当前不允许为 NULL，需要先修改
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) NULL COMMENT '手机号';

-- 注意：虽然数据库允许两个字段都为 NULL，但应用层逻辑会确保至少有一个不为 NULL
-- 这是为了支持用户可以选择使用邮箱或手机号注册

