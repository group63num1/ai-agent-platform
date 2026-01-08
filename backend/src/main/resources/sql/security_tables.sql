-- 安全与合规功能数据库表结构

-- 1. 敏感词表
CREATE TABLE IF NOT EXISTS `sensitive_words` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID',
  `word` varchar(100) NOT NULL COMMENT '敏感词',
  `category` varchar(50) DEFAULT NULL COMMENT '分类：violence-暴力, porn-色情, politics-政治, fraud-诈骗, privacy-隐私等',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '风险级别：1-低风险，2-中风险，3-高风险',
  `action` varchar(20) NOT NULL DEFAULT 'warn' COMMENT '处理动作：block-阻止, warn-警告, replace-替换',
  `replacement` varchar(100) DEFAULT NULL COMMENT '替换词（当action为replace时使用）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_category` (`category`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

-- 2. 安全规则表
CREATE TABLE IF NOT EXISTS `security_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `rule_name` varchar(100) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(20) NOT NULL COMMENT '规则类型：keyword-关键词, pattern-正则模式, ai-AI检测',
  `pattern` text NOT NULL COMMENT '匹配模式（正则表达式或关键词）',
  `category` varchar(50) DEFAULT NULL COMMENT '分类：crisis-危机, risk-风险, compliance-合规',
  `risk_level` tinyint NOT NULL DEFAULT '1' COMMENT '风险级别：1-低，2-中，3-高，4-严重',
  `action` varchar(20) NOT NULL DEFAULT 'warn' COMMENT '处理动作：block-阻止, warn-警告, alert-告警, review-人工审核',
  `warning_message` varchar(500) DEFAULT NULL COMMENT '警告消息',
  `guidance` text COMMENT '话术指导（建议的回复方式）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级（数字越大优先级越高）',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_rule_type` (`rule_type`),
  KEY `idx_category` (`category`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全规则表';

-- 3. 风险记录表
CREATE TABLE IF NOT EXISTS `risk_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint DEFAULT NULL COMMENT '会话ID',
  `message_id` bigint DEFAULT NULL COMMENT '消息ID',
  `risk_type` varchar(50) NOT NULL COMMENT '风险类型：sensitive-敏感词, crisis-危机, privacy-隐私泄露',
  `risk_level` tinyint NOT NULL COMMENT '风险级别：1-低，2-中，3-高，4-严重',
  `content` text COMMENT '触发风险的内容',
  `matched_rule` varchar(200) DEFAULT NULL COMMENT '匹配的规则（规则名称或敏感词）',
  `action` varchar(50) DEFAULT NULL COMMENT '处理动作：blocked-已阻止, warned-已警告, alerted-已告警, reviewed-已审核',
  `details` json DEFAULT NULL COMMENT '详细信息（JSON格式）',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COMMENT '用户代理',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_risk_type` (`risk_type`),
  KEY `idx_risk_level` (`risk_level`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险记录表';

-- 4. 隐私数据表
CREATE TABLE IF NOT EXISTS `privacy_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint DEFAULT NULL COMMENT '会话ID',
  `data_type` varchar(50) NOT NULL COMMENT '数据类型：phone-手机号, email-邮箱, idcard-身份证, bankcard-银行卡, address-地址等',
  `data_value` text NOT NULL COMMENT '数据值（加密存储）',
  `data_hash` varchar(64) NOT NULL COMMENT '数据哈希值（用于去重和检索）',
  `source` varchar(50) NOT NULL COMMENT '数据来源：user_input-用户输入, ai_response-AI回复, system-系统',
  `action` varchar(50) DEFAULT NULL COMMENT '操作类型：detected-检测到, stored-已存储, deleted-已删除, masked-已脱敏',
  `retention_days` int DEFAULT NULL COMMENT '保留天数（数据最小化策略）',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间（自动删除时间）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_data_type` (`data_type`),
  KEY `idx_data_hash` (`data_hash`),
  KEY `idx_expires_at` (`expires_at`),
  KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='隐私数据表';

-- 初始化数据：示例敏感词
INSERT INTO `sensitive_words` (`word`, `category`, `level`, `action`, `replacement`, `status`, `description`) VALUES
('暴力', 'violence', 3, 'block', NULL, 1, '暴力相关敏感词'),
('色情', 'porn', 3, 'block', NULL, 1, '色情相关敏感词'),
('政治', 'politics', 2, 'warn', NULL, 1, '政治相关敏感词'),
('诈骗', 'fraud', 3, 'block', NULL, 1, '诈骗相关敏感词');

-- 初始化数据：示例安全规则
INSERT INTO `security_rules` (`rule_name`, `rule_type`, `pattern`, `category`, `risk_level`, `action`, `warning_message`, `guidance`, `status`, `priority`, `description`) VALUES
('自杀倾向检测', 'keyword', '自杀|自残|不想活了', 'crisis', 4, 'alert', '检测到高风险内容，请及时关注用户心理健康', '建议引导用户寻求专业帮助，提供心理咨询热线', 1, 100, '检测用户可能的自杀倾向'),
('暴力威胁检测', 'pattern', '(杀|伤害|攻击).*(你|他|她|他们)', 'crisis', 4, 'alert', '检测到暴力威胁内容', '建议立即阻止并记录，必要时报警', 1, 90, '检测暴力威胁'),
('隐私信息泄露', 'pattern', '\\d{17}[\\dXx]|1[3-9]\\d{9}', 'privacy', 3, 'warn', '检测到可能包含隐私信息', '建议提醒用户注意隐私保护', 1, 50, '检测身份证号、手机号等隐私信息');

