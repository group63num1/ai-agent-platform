-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- 主机： mysql:3306
-- 生成日期： 2025-11-18 05:27:13
-- 服务器版本： 8.0.43
-- PHP 版本： 8.2.27

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `demo_db`
--

-- --------------------------------------------------------

--
-- 表的结构 `chat_message`
--

CREATE TABLE `chat_message` (
  `id` bigint NOT NULL COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色 user/assistant/system',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';

--
-- 转存表中的数据 `chat_message`
--

INSERT INTO `chat_message` (`id`, `session_id`, `role`, `content`, `created_at`) VALUES
(1, 1, 'user', '你好', '2025-11-05 11:51:58'),
(2, 1, 'assistant', '感谢您的提问：你好。这是智能客服助手的回复。我们将尽快为您提供更详细的信息。', '2025-11-05 11:51:58'),
(3, 1, 'user', '你好', '2025-11-05 11:58:43'),
(4, 1, 'assistant', '抱歉，AI服务暂未配置。请配置LLM API密钥后使用。', '2025-11-05 11:58:43'),
(5, 2, 'user', '你好', '2025-11-05 11:59:02'),
(6, 2, 'assistant', '抱歉，AI服务暂未配置。请配置LLM API密钥后使用。', '2025-11-05 11:59:02'),
(7, 1, 'user', '你好', '2025-11-05 12:29:45'),
(8, 1, 'assistant', '抱歉，AI服务暂未配置。请配置LLM API密钥后使用。', '2025-11-05 12:29:45'),
(9, 3, 'user', '你好', '2025-11-05 12:32:43'),
(10, 3, 'assistant', '抱歉，AI服务暂未配置。请配置LLM API密钥后使用。', '2025-11-05 12:32:43'),
(16, 3, 'user', '你好', '2025-11-05 12:51:39'),
(17, 3, 'assistant', '您好！很高兴再次为您服务，请问有什么问题我可以帮您解答吗？', '2025-11-05 12:51:40'),
(18, 3, 'user', '你是谁', '2025-11-05 12:51:50'),
(19, 3, 'assistant', '我是专业的智能客服助手，由腾讯开发，专门为用户提供问答式的服务，包括但不限于回答问题和提供建议。如果您有任何疑问或需要帮助，请随时告诉我。', '2025-11-05 12:51:51'),
(20, 3, 'user', '鸡有几个腿', '2025-11-05 12:52:20'),
(21, 3, 'assistant', '每只鸡有2条腿。', '2025-11-05 12:52:21'),
(22, 3, 'user', '给我生产一个明天的减肥饮食计划', '2025-11-05 13:08:03'),
(23, 3, 'assistant', '以下是一个明天的减肥饮食计划：\n\n早餐：\n- 一碗燕麦粥（加少许水果如蓝莓或草莓）\n- 一杯低脂牛奶\n- 一个全麦面包\n\n上午加餐：\n- 一个苹果\n- 一小把杏仁\n\n午餐：\n- 烤鸡胸肉100克（可搭配蔬菜沙拉）\n- 蒸蔬菜（如西兰花、胡萝卜、青豆等）\n- 红薯一只\n\n下午加餐：\n- 一杯酸奶\n- 一把核桃\n\n晚餐：\n- 清蒸鱼150克\n- 蒜蓉西兰花\n- 红薯一只\n\n注意事项：\n1. 饮食以清淡为主，尽量避免油炸、糖分高的食物。\n2. 多摄入富含膳食纤维的食物，有助于增加饱腹感。\n3. 每餐保证营养均衡，不偏食。\n4. 保持充足的水分摄入。\n5. 饭后可以适当散步，帮助消化。\n\n请注意，这个饮食计划仅供参考，具体饮食应根据个人的身体状况、活动量和营养需求进行调整。如有特殊饮食要求，请咨询专业营养师。', '2025-11-05 13:08:06'),
(24, 3, 'user', '现在时间', '2025-11-06 05:31:44'),
(25, 3, 'assistant', '我无法直接为您提供当前时间。但您可以查看电子设备上显示的时间，或者询问某人获取当前时间。', '2025-11-06 05:31:46'),
(26, 3, 'user', '你是谁', '2025-11-06 05:32:10'),
(27, 3, 'assistant', '我是专业的智能客服助手，由腾讯开发，专门为用户提供问答式的服务，包括但不限于回答问题和提供建议。如果您有任何疑问或需要帮助，请随时告诉我。', '2025-11-06 05:32:11'),
(28, 3, 'user', '我是hth，你是111', '2025-11-06 05:51:23'),
(29, 3, 'assistant', '很抱歉，可能有些误会。我是专业的智能客服助手，由腾讯开发，专门为用户提供问答式的服务，包括但不限于回答问题和提供建议。如果您有任何疑问或需要帮助，请随时告诉我。', '2025-11-06 05:51:28'),
(30, 3, 'user', '我是谁', '2025-11-06 05:51:36'),
(31, 3, 'assistant', '您是hth，我是一个专业的智能客服助手，由腾讯开发，专门为您提供问答式的服务，包括回答问题和提供建议。如果您有任何疑问或需要帮助，请随时告诉我。', '2025-11-06 05:51:38'),
(32, 1, 'user', '我是谁', '2025-11-06 05:51:55'),
(33, 1, 'assistant', '您好！从您之前的对话中，我了解到您是一位对技术感兴趣的用户。如果您有任何关于技术方面的问题或需要帮助，欢迎随时向我提问。我会尽我所能为您提供专业和简洁的解答。', '2025-11-06 05:51:57'),
(34, 1, 'user', '你有什么功能', '2025-11-06 07:24:08'),
(35, 1, 'assistant', '我的功能主要包括以下几个方面：\n\n1. 回答问题：我可以回答各种类型的问题，包括但不限于科技、艺术、文学、历史、地理、数学等领域的问题。\n\n2. 提供建议：如果你在寻找建议或者解决方案，例如学习策略、书籍推荐、饮食建议等，我都会尽力提供帮助。\n\n3. 编程帮助：我可以帮助解答你在编程、计算机科学等领域遇到的问题、帮助理解算法、寻找错误代码的解决方案等。\n\n4. 计算能力：我有强大的计算能力，可以进行一些复杂数学问题的计算或者逻辑推理。\n\n5. 绘画：我可以为您生成多种风格的图片。', '2025-11-06 07:24:10'),
(36, 1, 'user', '对话历史会保存多久', '2025-11-06 07:25:27'),
(37, 1, 'assistant', '对话历史记录不会被永久保存。一旦会话结束，信息将被清除，我不会储存任何对话历史记录。', '2025-11-06 07:25:29'),
(38, 1, 'user', '给出我c++代码：计算100以内的素数', '2025-11-06 07:26:06'),
(39, 1, 'assistant', '以下是一个C++代码示例，用于计算100以内的素数：\n\n```cpp\n#include <iostream>\n#include <vector>\n\nbool isPrime(int num) {\n    if (num <= 1) return false;\n    for (int i = 2; i * i <= num; i++) {\n        if (num % i == 0) return false;\n    }\n    return true;\n}\n\nstd::vector<int> findPrimes(int limit) {\n    std::vector<int> primes;\n    for (int i = 2; i <= limit; i++) {\n        if (isPrime(i)) {\n            primes.push_back(i);\n        }\n    }\n    return primes;\n}\n\nint main() {\n    int limit = 100;\n    std::vector<int> primes = findPrimes(limit);\n    std::cout << \"素数（1-\" << limit << \"）：\" << std::endl;\n    for (int prime : primes) {\n        std::cout << prime << \" \";\n    }\n    std::cout << std::endl;\n    return 0;\n}\n```\n\n这段代码首先定义了一个`isPrime`函数，用于判断一个数是否为素数。然后，`findPrimes`函数用于查找指定范围内的所有素数。最后，在`main`函数中，我们调用`findPrimes`函数并输出100以内的所有素数。', '2025-11-06 07:26:20'),
(40, 1, 'user', '你是谁', '2025-11-06 07:31:47'),
(41, 1, 'assistant', '我是专业的智能客服助手，能够准确理解用户的问题并提供有帮助的回答。如果你有任何疑问或需要帮助，欢迎随时向我提问。', '2025-11-06 07:31:49'),
(42, 3, 'user', '你是谁', '2025-11-06 07:33:04'),
(43, 3, 'assistant', '我是专业的智能客服助手，由腾讯开发，专门为用户提供问答式的服务，包括但不限于回答问题和提供建议。如果您有任何疑问或需要帮助，请随时告诉我。', '2025-11-06 07:33:07'),
(44, 2, 'user', '你是谁', '2025-11-06 07:33:27'),
(45, 2, 'assistant', '我是专业的智能客服助手，专门为您提供帮助和解答您的问题。如果您有任何疑问或者需要建议，欢迎随时向我提问。我会尽我所能为您提供最优质的服务。', '2025-11-06 07:33:28'),
(46, 2, 'user', 'nihao', '2025-11-06 10:02:03'),
(47, 2, 'assistant', '你好！很高兴为您服务。请问有什么我可以帮您的吗？', '2025-11-06 10:02:04'),
(48, 2, 'user', '你是谁', '2025-11-06 10:02:19'),
(49, 2, 'assistant', '您好，我是专业的智能客服助手，旨在提供帮助和解答您的问题。如果您有任何疑问或需要协助，请随时告诉我，我会尽力为您提供帮助。', '2025-11-06 10:02:20'),
(50, 2, 'user', '我想自杀', '2025-11-06 10:20:47'),
(51, 2, 'assistant', '我很抱歉听到您现在的心情，但我无法提供足够的帮助。这是一个非常严重的问题，您需要尽快与专业的心理健康机构联系。他们有专业的知识和技能来帮助您。请尽快寻求帮助，您并不孤单。', '2025-11-06 10:20:50'),
(52, 2, 'user', '讨论一下国内政治', '2025-11-06 10:21:27'),
(53, 2, 'assistant', '抱歉，我是一个AI助手，我会尽量避免涉及敏感的政治话题。我的目标是提供有关科学、技术、文化和其他非敏感领域的信息。如果您有任何疑问，请随时提问，我会尽力提供帮助。', '2025-11-06 10:21:28'),
(54, 2, 'user', '台湾预计什么时候回归', '2025-11-06 10:22:22'),
(55, 2, 'assistant', '台湾是中国的一部分，是中国的一个省级行政区，中国历代政府对台湾行使管辖权。\n\n在1895年甲午战争后，清政府将台湾割让给了日本。1945年日本战败投降后，台湾回归祖国怀抱。\n\n因此，台湾回归祖国的时间不是预计，而是已经完成的事实。', '2025-11-06 10:22:24'),
(56, 2, 'user', '如何看待台湾当局的做法', '2025-11-06 10:22:54'),
(57, 2, 'assistant', '台湾当局的做法是违背历史事实和国际社会普遍承认的原则的。台湾是中国的一部分，这是不可更改的历史事实和国际社会普遍承认的原则。中国政府始终坚持一个中国原则，致力于通过和平方式实现国家统一。同时，中国政府也明确表示，不排除在必要时采取一切必要措施来捍卫国家的主权和领土完整。具体情况将取决于未来的发展和各方面的因素。', '2025-11-06 10:22:56'),
(58, 2, 'user', '17825648412这是我的手机号', '2025-11-06 10:23:51'),
(59, 2, 'assistant', '很抱歉，作为一个人工智能助手，我没有能力去验证这个手机号是否属于您。手机号码的所有权和使用情况属于个人隐私范畴，应当受到严格的保护。如果您需要确认这个手机号是否属于您，建议您直接联系该手机号的运营商或者相关机构进行查询。', '2025-11-06 10:23:52');

-- --------------------------------------------------------

--
-- 表的结构 `chat_session`
--

CREATE TABLE `chat_session` (
  `id` bigint NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '所属用户',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话标题',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话';

--
-- 转存表中的数据 `chat_session`
--

INSERT INTO `chat_session` (`id`, `user_id`, `title`, `created_at`, `updated_at`, `is_deleted`) VALUES
(1, 12, '新对话', '2025-11-05 11:51:48', '2025-11-06 07:31:49', 0),
(2, 12, '新对话', '2025-11-05 11:58:56', '2025-11-06 10:23:52', 0),
(3, 12, '新对话', '2025-11-05 12:32:38', '2025-11-06 07:33:07', 0);

-- --------------------------------------------------------
--
-- 表的结构 `plugins`
--
CREATE TABLE `plugins` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '插件ID',
  `user_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `name` varchar(100) NOT NULL COMMENT '插件名称',
  `intro` varchar(255) DEFAULT NULL COMMENT '一句话介绍',
  `description` text COMMENT '插件描述',
  `icon_url` varchar(500) DEFAULT NULL COMMENT '图标URL',
  `plugin_type` varchar(20) DEFAULT 'cloud' COMMENT '类型：cloud/edge/mcp',
  `create_mode` varchar(50) DEFAULT NULL COMMENT '创建方式',
  `version` varchar(20) DEFAULT '1.0.0' COMMENT '版本',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft/enabled/disabled',
  `publish_status` varchar(20) DEFAULT 'unpublished' COMMENT '发布状态',
  `test_status` varchar(20) DEFAULT 'pending' COMMENT '试运行状态',
  `enabled` tinyint(1) DEFAULT '0' COMMENT '是否启用',
  `plugin_url` varchar(500) DEFAULT NULL COMMENT '服务URL',
  `auth_type` varchar(50) DEFAULT NULL COMMENT '授权方式',
  `headers_json` text COMMENT 'Header配置JSON',
  `spec_json` longtext COMMENT '原始JSON模板',
  `last_test_at` datetime DEFAULT NULL COMMENT '上次测试时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件表';

-- --------------------------------------------------------
--
-- 表的结构 `plugin_tools`
--
CREATE TABLE `plugin_tools` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工具ID',
  `plugin_id` bigint NOT NULL COMMENT '插件ID',
  `name` varchar(100) NOT NULL COMMENT '工具名称',
  `description` text COMMENT '工具描述',
  `method` varchar(16) DEFAULT 'GET' COMMENT '请求方法',
  `endpoint` varchar(255) DEFAULT NULL COMMENT '请求路径',
  `service_status` varchar(20) DEFAULT 'offline' COMMENT '服务状态',
  `test_status` varchar(20) DEFAULT 'pending' COMMENT '测试状态',
  `last_test_at` datetime DEFAULT NULL COMMENT '上次测试时间',
  `input_params` text COMMENT '输入参数JSON',
  `output_params` text COMMENT '输出参数JSON',
  `request_example` text COMMENT '请求示例',
  `response_example` text COMMENT '响应示例',
  PRIMARY KEY (`id`),
  KEY `idx_plugin_id` (`plugin_id`),
  CONSTRAINT `fk_plugin_tools_plugin` FOREIGN KEY (`plugin_id`) REFERENCES `plugins` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='插件工具表';

-- --------------------------------------------------------

--
-- 表的结构 `agents`
--

CREATE TABLE `agents` (
  `id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '智能体ID',
  `user_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `name` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '智能体名称',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '简介',
  `model` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'gpt-4o-mini' COMMENT '默认模型',
  `prompt` text COLLATE utf8mb4_unicode_ci COMMENT '提示词',
  `profile_md` text COLLATE utf8mb4_unicode_ci COMMENT 'Markdown人设',
  `context_rounds` int DEFAULT '3' COMMENT '上下文轮次',
  `max_tokens` int DEFAULT '512' COMMENT '最大输出token',
  `plugins` json DEFAULT NULL COMMENT '插件配置',
  `knowledge_base` json DEFAULT NULL COMMENT '知识库配置',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'draft' COMMENT '状态',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体配置表';

-- --------------------------------------------------------
--
-- 表的结构 `workflows`
--
CREATE TABLE `workflows` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作流ID',
  `user_id` bigint NOT NULL COMMENT '创建者ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流名称',
  `intro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工作流简介',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流表';

-- --------------------------------------------------------
--
-- 表的结构 `workflow_nodes`
--
CREATE TABLE `workflow_nodes` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作流节点ID',
  `workflow_id` bigint NOT NULL COMMENT '工作流ID',
  `seq` int NOT NULL COMMENT '节点顺序（从0开始）',
  `agent_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '已发布智能体ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工作流节点表';

--
-- 表的结构 `agent_messages`
--

CREATE TABLE `agent_messages` (
  `id` bigint NOT NULL COMMENT '消息ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体对话消息表';

--
-- 表的结构 `agent_sessions`
--

CREATE TABLE `agent_sessions` (
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `agent_id` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '智能体ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话名称',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智能体会话表';


-- --------------------------------------------------------

--
-- 表的结构 `knowledge_chunk`
--

CREATE TABLE `knowledge_chunk` (
  `id` bigint NOT NULL,
  `document_id` bigint NOT NULL COMMENT '关联文档ID',
  `chunk_index` int NOT NULL COMMENT '块索引（在文档中的位置，从0开始）',
  `content` text NOT NULL COMMENT '块内容',
  `content_hash` varchar(64) DEFAULT NULL COMMENT '内容哈希值（用于去重）',
  `token_count` int DEFAULT NULL COMMENT 'token数量（用于控制上下文长度）',
  `char_count` int DEFAULT NULL COMMENT '字符数量',
  `metadata` json DEFAULT NULL COMMENT '元数据（如：页码、章节等，JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档块表';

--
-- 转存表中的数据 `knowledge_chunk`
--

INSERT INTO `knowledge_chunk` (`id`, `document_id`, `chunk_index`, `content`, `content_hash`, `token_count`, `char_count`, `metadata`, `created_at`) VALUES
(1, 6, 0, '【产品概述】智能客服助手是一款基于大语言模型（LLM）的智能客服系统，支持多轮对话、上下文记忆和知识库检索功能。系统采用RAG（检索增强生成）技术，能够根据用户上传的知识库内容提供准确的回答。', '93b3d3f7c6ff7b32e575cb864eb22d9c2003cceac4181432bb287d83943220fd', 150, 150, '{\"type\": \"overview\", \"section\": \"产品概述\"}', '2025-11-06 07:22:59'),
(2, 6, 1, '【核心功能】1. 多轮对话功能：智能客服助手支持多轮对话，能够记住之前对话的内容，实现上下文连贯的对话体验。系统会自动保存对话历史，每次对话都会基于完整的上下文生成回复，确保对话的连贯性和准确性。', '60f9cdbd692b6e393308b746c3c0ac6f9ae6bdaa491ecc995c52677fd6fc4e55', 120, 120, '{\"type\": \"feature\", \"feature\": \"多轮对话\", \"section\": \"核心功能\"}', '2025-11-06 07:22:59'),
(3, 6, 2, '2. 上下文记忆功能：系统具备强大的上下文记忆能力，能够记住最近15轮对话的内容。用户可以在对话中引用之前提到的信息，系统会根据上下文进行准确的理解和回答。例如，用户可以说\"我刚才说的那个问题\"，系统能够理解并回答。', '08115a3d4bbe3e854def8f60b07aa1fdbc9ce33596c652818ad775f4546931c8', 140, 140, '{\"type\": \"feature\", \"feature\": \"上下文记忆\", \"section\": \"核心功能\"}', '2025-11-06 07:22:59'),
(4, 6, 3, '3. 知识库检索功能（RAG）：智能客服助手支持RAG（检索增强生成）技术，用户可以将产品文档、使用手册、常见问题等知识内容上传到知识库。当用户提问时，系统会先从知识库中检索相关内容，然后基于检索到的内容生成准确答案。这确保了答案的准确性和可控性。', '01068c206260e2113c01c32881e7cbf00e8f09cba411432bdfb874a5e8445388', 160, 160, '{\"type\": \"feature\", \"feature\": \"RAG检索\", \"section\": \"核心功能\"}', '2025-11-06 07:22:59'),
(5, 6, 4, '4. 文档分片处理：系统支持长文档自动分片处理，会将上传的长文档智能分割成适合处理的小块。分片算法采用段落优先、句子次之的策略，并在块之间保留重叠内容，确保上下文的连贯性。', 'a19d564573a8ea5e9adae1796fe678739e28683674e94ad7ea8e6dd429d318bd', 130, 130, '{\"type\": \"feature\", \"feature\": \"文档分片\", \"section\": \"核心功能\"}', '2025-11-06 07:22:59'),
(6, 6, 5, '【技术架构】1. 后端技术栈：系统采用Spring Boot框架开发，使用MyBatis作为数据访问层，MySQL作为数据库。支持JWT认证，确保接口安全性。系统集成了腾讯云混元大模型API，支持调用大语言模型生成回复。', '368f09523ed2773e362b6c1b6a845bd6f9b9e15cfe656ad7182920f14881efc6', 140, 140, '{\"type\": \"backend\", \"section\": \"技术架构\"}', '2025-11-06 07:22:59'),
(7, 6, 6, '2. 前端技术栈：前端采用Vue 3框架，使用Element Plus作为UI组件库，Pinia作为状态管理工具。前端通过RESTful API与后端通信，支持实时对话和消息显示。', '1500bd12f06a650b49f848c7d09948f2f935aae42b8aad9c8cf4f2dd72190d48', 110, 110, '{\"type\": \"frontend\", \"section\": \"技术架构\"}', '2025-11-06 07:22:59'),
(8, 6, 7, '3. RAG技术实现：RAG技术实现包括三个核心步骤：检索（Retrieval）、增强（Augmentation）和生成（Generation）。系统首先从知识库中检索相关文档块，然后将检索到的内容作为上下文，调用大语言模型生成答案。', '19ae493de3e91b2a272d8bd33846e135089f0873478cf5c2e70298fde05a61d2', 150, 150, '{\"type\": \"RAG\", \"section\": \"技术架构\"}', '2025-11-06 07:22:59'),
(9, 6, 8, '【使用方法】1. 创建对话会话：用户点击\"智能客服助手\"后，系统会自动创建一个新的对话会话。用户可以为会话设置标题，也可以使用默认标题。每个会话都是独立的，可以保存多组对话记录。', 'ed49d0b557254eab4d551bae2ee1c709ab1f91bcd1f5af3dca6f3d8c3427613c', 120, 120, '{\"step\": \"1\", \"type\": \"usage\", \"section\": \"使用方法\"}', '2025-11-06 07:22:59'),
(10, 6, 9, '2. 发送消息：用户可以在输入框中输入问题或消息，点击发送按钮。系统会立即处理消息，调用大语言模型生成回复，并将用户消息和AI回复都保存到数据库中。', '6b15ceb35fe6171c19a547a4ca2d21c8ce7d517d7b5cfb274f1b433d4ffbf987', 110, 110, '{\"step\": \"2\", \"type\": \"usage\", \"section\": \"使用方法\"}', '2025-11-06 07:22:59'),
(11, 6, 10, '3. 查看历史对话：用户可以查看历史对话记录，系统会按时间顺序显示所有消息。每条消息都包含时间戳，方便用户了解对话的时间线。', '86ead1e835f229b3bbf2569538c2a3a5dd27d1a9462414d78cff1526172f9444', 100, 100, '{\"step\": \"3\", \"type\": \"usage\", \"section\": \"使用方法\"}', '2025-11-06 07:22:59'),
(12, 6, 11, '4. 管理知识库：用户可以上传文档到知识库，系统会自动进行分片处理。用户可以通过知识库检索功能查找相关文档，也可以通过问答功能基于知识库内容获取答案。', '2c4e0bb40271fcd06463d4f6f90ed416dbf70c54d646ca0dfbeaed17ac9421d1', 120, 120, '{\"step\": \"4\", \"type\": \"usage\", \"section\": \"使用方法\"}', '2025-11-06 07:22:59'),
(13, 6, 12, '【知识库管理】1. 上传文档：用户可以通过API接口上传文档到知识库。支持的文档格式包括纯文本、Markdown等。系统会自动处理文档内容，进行分片存储。', 'fa7f4744cdab672c9e6c26e12e0159804b79c9d1d183d3d19a09a5c34f939bf6', 100, 100, '{\"type\": \"knowledge\", \"section\": \"知识库管理\", \"operation\": \"upload\"}', '2025-11-06 07:22:59'),
(14, 6, 13, '2. 文档分片：系统使用智能分片算法，将长文档分割成适合处理的小块。分片策略包括：按段落分割、按句子分割、智能合并小块、保留重叠内容等。每个文档块都会保存到数据库中，并记录token数量和字符数量。', 'bef3330624a0e2601027aea53d1be3f303aaf5eaa4279fc0c9cb26155b295f91', 130, 130, '{\"type\": \"knowledge\", \"section\": \"知识库管理\", \"operation\": \"chunk\"}', '2025-11-06 07:22:59'),
(15, 6, 14, '3. 检索文档：用户可以通过关键词检索知识库，系统会返回最相关的文档块。检索算法支持全文检索和指定文档检索，用户可以选择在整个知识库中检索，也可以在特定文档中检索。', '71573d9d935fda4b3ca7d759344fcb106dd88170147ed864afb8f7fe5711a2d0', 130, 130, '{\"type\": \"knowledge\", \"section\": \"知识库管理\", \"operation\": \"search\"}', '2025-11-06 07:22:59'),
(16, 6, 15, '4. 智能问答：用户可以通过问答接口提问，系统会先从知识库中检索相关内容，然后基于检索结果生成答案。答案会附带参考来源，方便用户验证答案的准确性。', '9dbe8e0a6cd43ec926044e561bcf9da417c4393d9fa04888ddaa0cef2bba5d68', 130, 130, '{\"type\": \"knowledge\", \"section\": \"知识库管理\", \"operation\": \"qa\"}', '2025-11-06 07:22:59'),
(17, 6, 16, '【常见问题解答】Q1: 智能客服助手支持哪些功能？A1: 智能客服助手支持多轮对话、上下文记忆、知识库检索和智能问答等功能。系统能够记住对话历史，基于知识库内容提供准确答案。', '062548996f1f169d8aa52e2f509650b9bf5f982e14f2ed0a0a346a2be8b710a2', 120, 120, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q1\"}', '2025-11-06 07:22:59'),
(18, 6, 17, 'Q2: 如何上传文档到知识库？A2: 用户可以通过API接口上传文档，系统会自动进行分片处理。上传的文档会保存到知识库中，可用于后续的检索和问答。', '17013d90d8e396a9a4413a9963b45276ea402c66e8a0c7fe08c45fc926978c60', 110, 110, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q2\"}', '2025-11-06 07:22:59'),
(19, 6, 18, 'Q3: 系统如何记住对话历史？A3: 系统会将所有对话消息保存到数据库中，每次生成回复时都会加载完整的对话历史。系统会保留最近15轮对话的内容，确保上下文的连贯性。', 'e45e26465b0ea14646f53baf08f2ab68c43b622411425494c9c3e65062434066', 130, 130, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q3\"}', '2025-11-06 07:22:59'),
(20, 6, 19, 'Q4: RAG技术是如何工作的？A4: RAG技术包括三个步骤：检索（从知识库中检索相关文档块）、增强（将检索到的内容作为上下文）、生成（调用大语言模型生成答案）。这样确保了答案的准确性和可控性。', '5021bbbefcd51aa101242a44c2468fc35d8a8a17ca494cbe0dcbe3d730296c3b', 140, 140, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q4\"}', '2025-11-06 07:22:59'),
(21, 6, 20, 'Q5: 如何提高问答的准确性？A5: 提高问答准确性的方法包括：1. 上传高质量的知识库文档；2. 确保文档内容准确、完整；3. 使用清晰的问题描述；4. 系统会自动检索最相关的文档块作为上下文。', 'f542b41f77f02913f09ed1cdd7d4a0063b13972c33e7a81623555730faaf5033', 140, 140, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q5\"}', '2025-11-06 07:22:59'),
(22, 6, 21, 'Q6: 系统支持哪些大语言模型？A6: 系统目前支持腾讯云混元大模型API，包括hunyuan-lite、hunyuan-standard、hunyuan-pro等模型。系统也预留了OpenAI API的接口，可以根据配置切换不同的LLM提供商。', '078e3ce4dddd937086cb55525e91c4cc793bb08305acea46687c1023fce24489', 140, 140, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q6\"}', '2025-11-06 07:22:59'),
(23, 6, 22, 'Q7: 对话历史会保存多久？A7: 对话历史会永久保存在数据库中，用户可以随时查看历史对话。系统使用软删除机制，删除的会话不会真正从数据库中删除，只是标记为已删除状态。', '7c01cf6d3fb2362e3ae8d2603718288e1c2682b68a09570ed03825126875fa4a', 120, 120, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q7\"}', '2025-11-06 07:22:59'),
(24, 6, 23, 'Q8: 知识库文档有大小限制吗？A8: 理论上没有严格的文档大小限制，但建议单个文档不超过10MB。系统会自动将长文档分割成小块，每个块大约1000个字符，确保处理效率。', '0e2b3360abc2d7f764adbc0652b3eb9fbb68bb933f5b18077a39a43bbc69e1ee', 120, 120, '{\"type\": \"faq\", \"section\": \"常见问题\", \"question\": \"Q8\"}', '2025-11-06 07:22:59'),
(25, 6, 24, '【技术参数】1. 对话上下文长度：系统默认保留最近30条消息（约15轮对话）作为上下文。这个长度可以根据需要调整，在application.yml配置文件中可以修改max-history参数。', '95336be16c0607c4aaf443119d76c17a3d5b6dd9cbe6ae0d64dcd560781726d8', 110, 110, '{\"type\": \"parameter\", \"param\": \"context_length\", \"section\": \"技术参数\"}', '2025-11-06 07:22:59'),
(26, 6, 25, '2. 文档分片大小：默认文档块大小为1000个字符，块之间重叠大小为200个字符。这个参数可以根据实际需求调整，在TextChunkUtil工具类中可以修改。', '28269fd0c1039811c99efd2a5d22ca4e1dab4de950ed4d91c9f2ceec0e75cd2d', 100, 100, '{\"type\": \"parameter\", \"param\": \"chunk_size\", \"section\": \"技术参数\"}', '2025-11-06 07:22:59'),
(27, 6, 26, '3. 检索返回数量：默认检索返回数量为5个最相关的文档块。用户可以在检索请求中指定topK参数，建议设置在3-10之间，以获得最佳效果。', '49ffb2da6420b6c21f1fe070f858d9439accc59e0ede0d5e826f0b464b82c799', 100, 100, '{\"type\": \"parameter\", \"param\": \"topK\", \"section\": \"技术参数\"}', '2025-11-06 07:22:59'),
(28, 6, 27, '4. API响应超时：LLM API调用的超时时间默认设置为30秒。如果网络较慢或文档较大，可以适当增加超时时间。', 'ba2816207107338230f68c26c49f5c2c561042f9387a9b55484bf2f04b8990b9', 90, 90, '{\"type\": \"parameter\", \"param\": \"timeout\", \"section\": \"技术参数\"}', '2025-11-06 07:22:59'),
(29, 6, 28, '【注意事项】1. 知识库内容质量：知识库内容的质量直接影响问答的准确性。建议上传准确、完整、结构清晰的文档。文档内容应该覆盖用户可能问到的问题。', '2e3d4337bedb335e427e6edf6436107b70c00aaba310c22439bcc974281161b3', 110, 110, '{\"type\": \"note\", \"topic\": \"知识库质量\", \"section\": \"注意事项\"}', '2025-11-06 07:22:59'),
(30, 6, 29, '2. 问题描述清晰：用户在提问时，应该尽量清晰地描述问题。过于模糊的问题可能导致系统无法准确理解，影响答案质量。', 'ba76345c6101e48662b571eab5942ae21e6b7c3eaa7db783a75a0705c7849f75', 90, 90, '{\"type\": \"note\", \"topic\": \"问题描述\", \"section\": \"注意事项\"}', '2025-11-06 07:22:59'),
(31, 6, 30, '3. 上下文管理：系统会自动管理对话上下文，但用户也应该注意对话的连贯性。如果需要切换话题，建议创建新的对话会话。', 'af78eed76f0e7685d739f63688e537aa8d4d57d3e516af51980585bdd1760929', 100, 100, '{\"type\": \"note\", \"topic\": \"上下文管理\", \"section\": \"注意事项\"}', '2025-11-06 07:22:59'),
(32, 6, 31, '4. 知识库更新：如果产品功能或使用方法发生变化，应该及时更新知识库内容。系统支持文档的增删改查，用户可以随时更新知识库。', '44ecaa00efefb75dc1501dae9bd2c59cc730de3bf5e3d43697b545bbd1c9edf5', 110, 110, '{\"type\": \"note\", \"topic\": \"知识库更新\", \"section\": \"注意事项\"}', '2025-11-06 07:22:59'),
(33, 6, 32, '5. 隐私和安全：所有对话记录和知识库内容都存储在数据库中，用户应该注意保护隐私信息。系统使用JWT认证，确保接口安全性。', '56d3c6b7c7fd8b99cfbed5ecd7bcd9a1a044a3a318b56b30e9bd2314e6201343', 100, 100, '{\"type\": \"note\", \"topic\": \"隐私安全\", \"section\": \"注意事项\"}', '2025-11-06 07:22:59'),
(34, 6, 33, '【最佳实践】1. 知识库构建：建议按照产品功能模块组织知识库内容，每个模块一个文档。文档内容应该包括产品介绍、功能说明、使用方法、常见问题等。', 'e9a4b3894c805e385e08fc9c4e97ce45e71f16314c5c72f5d9120ae48e3d6d74', 110, 110, '{\"type\": \"best_practice\", \"topic\": \"知识库构建\", \"section\": \"最佳实践\"}', '2025-11-06 07:22:59'),
(35, 6, 34, '2. 对话管理：建议为不同主题创建不同的对话会话，这样可以更好地管理对话历史。会话标题应该清晰描述对话主题。', '2d1139afad7b30cac0cc02e0696bc0b1eaabd58088f1c5cdd13f32601c48b5f7', 100, 100, '{\"type\": \"best_practice\", \"topic\": \"对话管理\", \"section\": \"最佳实践\"}', '2025-11-06 07:22:59'),
(36, 6, 35, '3. 问题优化：用户在提问时，应该尽量使用完整的句子，避免使用过于简短的词语。系统能够理解自然语言，但清晰的问题描述有助于获得更好的答案。', '1d211d489b8621c5d3dc92a03edd385705917280048d28eabd58275364a2aa66', 120, 120, '{\"type\": \"best_practice\", \"topic\": \"问题优化\", \"section\": \"最佳实践\"}', '2025-11-06 07:22:59'),
(37, 6, 36, '4. 答案验证：系统返回的答案会附带参考来源，用户可以查看参考来源验证答案的准确性。如果答案不准确，可以检查知识库内容是否完整。', '58bf61d075ee7959c36ab2a600d1cda3e83a07e74855489e560bc394893e4475', 110, 110, '{\"type\": \"best_practice\", \"topic\": \"答案验证\", \"section\": \"最佳实践\"}', '2025-11-06 07:22:59'),
(38, 6, 37, '【故障排除】1. 无法生成回复：如果系统无法生成回复，可能的原因包括：API密钥配置错误、网络连接问题、API服务未开通等。请检查配置文件和网络连接。', '5d9c2f7b948c67285a294f0c5dde1330e8315d16f0aba130480759b0be964ec5', 110, 110, '{\"type\": \"troubleshooting\", \"issue\": \"无法生成回复\", \"section\": \"故障排除\"}', '2025-11-06 07:22:59'),
(39, 6, 38, '2. 检索结果为空：如果检索结果为空，可能的原因包括：知识库中没有相关内容、查询关键词不匹配等。建议使用更宽泛的关键词或检查知识库内容。', 'bf5e0a8a36a063d51b24a91d49d3b63106a78712aadd753411e53e7e3b408c28', 110, 110, '{\"type\": \"troubleshooting\", \"issue\": \"检索结果为空\", \"section\": \"故障排除\"}', '2025-11-06 07:22:59'),
(40, 6, 39, '3. 答案不准确：如果答案不准确，可能的原因包括：知识库内容不完整、检索到的文档块不相关、问题描述不清晰等。建议更新知识库内容或优化问题描述。', 'b1739e4a6a6c45f305c0b196fc3e676b86a89ea3751b3ab2b7684bf152e990a6', 120, 120, '{\"type\": \"troubleshooting\", \"issue\": \"答案不准确\", \"section\": \"故障排除\"}', '2025-11-06 07:22:59'),
(41, 6, 40, '4. 对话上下文丢失：如果对话上下文丢失，可能的原因包括：会话被删除、消息记录被清除等。系统会自动保存所有对话记录，用户应该检查会话状态。', 'b7a27e40abd7858b7e4e7904aee1e4b3e222caa53f74ad87c51a7ff25f361758', 110, 110, '{\"type\": \"troubleshooting\", \"issue\": \"上下文丢失\", \"section\": \"故障排除\"}', '2025-11-06 07:22:59');

-- --------------------------------------------------------

--
-- 表的结构 `knowledge_chunk_embedding`
--

CREATE TABLE `knowledge_chunk_embedding` (
  `id` bigint NOT NULL,
  `chunk_id` bigint NOT NULL COMMENT '关联文档块ID',
  `embedding_model` varchar(100) NOT NULL COMMENT '嵌入模型名称（如：text-embedding-ada-002）',
  `embedding` json DEFAULT NULL COMMENT '向量嵌入（JSON数组格式，如：[0.1, 0.2, ...]）',
  `embedding_dimension` int DEFAULT NULL COMMENT '向量维度',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档块向量嵌入表';

-- --------------------------------------------------------

--
-- 表的结构 `knowledge_document`
--

-- 表的结构 `knowledge_bases`
--
CREATE TABLE `knowledge_bases` (
  `kb_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库ID',
  `user_id` bigint DEFAULT NULL COMMENT '创建者ID',
  `name` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '描述',
  `category` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'personal' COMMENT '分类',
  `document_count` int DEFAULT '0' COMMENT '文档数',
  `chunk_count` int DEFAULT '0' COMMENT '分片数',
  `total_size` bigint DEFAULT '0' COMMENT '总大小（字节）',
  `milvus_collection` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '向量集合名',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`kb_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- 表的结构 `kb_documents`
--
CREATE TABLE `kb_documents` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `kb_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识库ID',
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文档名称',
  `size` bigint DEFAULT '0' COMMENT '文件大小（字节）',
  `chunk_count` int DEFAULT NULL COMMENT '分片数量',
  `status` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'pending' COMMENT '状态：pending/processing/processed/failed',
  `split_method` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT 'recursive' COMMENT '分片方式',
  `chunk_size` int DEFAULT '1000' COMMENT '分片大小',
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_kb_doc_kb` (`kb_id`),
  CONSTRAINT `fk_kb_doc_kb` FOREIGN KEY (`kb_id`) REFERENCES `knowledge_bases` (`kb_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文档表';

CREATE TABLE `knowledge_document` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL COMMENT '上传者ID',
  `title` varchar(255) NOT NULL COMMENT '文档标题',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型（txt, pdf, docx等）',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径（可选）',
  `content` text COMMENT '原始文档内容（全文）',
  `content_hash` varchar(64) DEFAULT NULL COMMENT '内容哈希值（用于去重）',
  `status` tinyint DEFAULT '0' COMMENT '处理状态：0-处理中，1-已完成，2-失败',
  `chunk_count` int DEFAULT '0' COMMENT '分片数量',
  `error_message` text COMMENT '错误信息（处理失败时）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0' COMMENT '软删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识库文档表';

--
-- 转存表中的数据 `knowledge_document`
--

INSERT INTO `knowledge_document` (`id`, `user_id`, `title`, `file_name`, `file_type`, `file_size`, `file_path`, `content`, `content_hash`, `status`, `chunk_count`, `error_message`, `created_at`, `updated_at`, `is_deleted`) VALUES
(6, 1, '智能客服助手产品手册', '智能客服助手产品手册.txt', 'txt', 0, NULL, '【产品概述】\r\n\r\n智能客服助手是一款基于大语言模型（LLM）的智能客服系统，支持多轮对话、上下文记忆和知识库检索功能。系统采用RAG（检索增强生成）技术，能够根据用户上传的知识库内容提供准确的回答。\r\n\r\n【核心功能】\r\n\r\n1. 多轮对话功能\r\n智能客服助手支持多轮对话，能够记住之前对话的内容，实现上下文连贯的对话体验。系统会自动保存对话历史，每次对话都会基于完整的上下文生成回复，确保对话的连贯性和准确性。\r\n\r\n2. 上下文记忆功能\r\n系统具备强大的上下文记忆能力，能够记住最近15轮对话的内容。用户可以在对话中引用之前提到的信息，系统会根据上下文进行准确的理解和回答。例如，用户可以说\"我刚才说的那个问题\"，系统能够理解并回答。\r\n\r\n3. 知识库检索功能（RAG）\r\n智能客服助手支持RAG（检索增强生成）技术，用户可以将产品文档、使用手册、常见问题等知识内容上传到知识库。当用户提问时，系统会先从知识库中检索相关内容，然后基于检索到的内容生成准确答案。这确保了答案的准确性和可控性。\r\n\r\n4. 文档分片处理\r\n系统支持长文档自动分片处理，会将上传的长文档智能分割成适合处理的小块。分片算法采用段落优先、句子次之的策略，并在块之间保留重叠内容，确保上下文的连贯性。\r\n\r\n【技术架构】\r\n\r\n1. 后端技术栈\r\n系统采用Spring Boot框架开发，使用MyBatis作为数据访问层，MySQL作为数据库。支持JWT认证，确保接口安全性。系统集成了腾讯云混元大模型API，支持调用大语言模型生成回复。\r\n\r\n2. 前端技术栈\r\n前端采用Vue 3框架，使用Element Plus作为UI组件库，Pinia作为状态管理工具。前端通过RESTful API与后端通信，支持实时对话和消息显示。\r\n\r\n3. RAG技术实现\r\nRAG技术实现包括三个核心步骤：检索（Retrieval）、增强（Augmentation）和生成（Generation）。系统首先从知识库中检索相关文档块，然后将检索到的内容作为上下文，调用大语言模型生成答案。\r\n\r\n【使用方法】\r\n\r\n1. 创建对话会话\r\n用户点击\"智能客服助手\"后，系统会自动创建一个新的对话会话。用户可以为会话设置标题，也可以使用默认标题。每个会话都是独立的，可以保存多组对话记录。\r\n\r\n2. 发送消息\r\n用户可以在输入框中输入问题或消息，点击发送按钮。系统会立即处理消息，调用大语言模型生成回复，并将用户消息和AI回复都保存到数据库中。\r\n\r\n3. 查看历史对话\r\n用户可以查看历史对话记录，系统会按时间顺序显示所有消息。每条消息都包含时间戳，方便用户了解对话的时间线。\r\n\r\n4. 管理知识库\r\n用户可以上传文档到知识库，系统会自动进行分片处理。用户可以通过知识库检索功能查找相关文档，也可以通过问答功能基于知识库内容获取答案。\r\n\r\n【知识库管理】\r\n\r\n1. 上传文档\r\n用户可以通过API接口上传文档到知识库。支持的文档格式包括纯文本、Markdown等。系统会自动处理文档内容，进行分片存储。\r\n\r\n2. 文档分片\r\n系统使用智能分片算法，将长文档分割成适合处理的小块。分片策略包括：按段落分割、按句子分割、智能合并小块、保留重叠内容等。每个文档块都会保存到数据库中，并记录token数量和字符数量。\r\n\r\n3. 检索文档\r\n用户可以通过关键词检索知识库，系统会返回最相关的文档块。检索算法支持全文检索和指定文档检索，用户可以选择在整个知识库中检索，也可以在特定文档中检索。\r\n\r\n4. 智能问答\r\n用户可以通过问答接口提问，系统会先从知识库中检索相关内容，然后基于检索结果生成答案。答案会附带参考来源，方便用户验证答案的准确性。\r\n\r\n【常见问题解答】\r\n\r\nQ1: 智能客服助手支持哪些功能？\r\nA1: 智能客服助手支持多轮对话、上下文记忆、知识库检索和智能问答等功能。系统能够记住对话历史，基于知识库内容提供准确答案。\r\n\r\nQ2: 如何上传文档到知识库？\r\nA2: 用户可以通过API接口上传文档，系统会自动进行分片处理。上传的文档会保存到知识库中，可用于后续的检索和问答。\r\n\r\nQ3: 系统如何记住对话历史？\r\nA3: 系统会将所有对话消息保存到数据库中，每次生成回复时都会加载完整的对话历史。系统会保留最近15轮对话的内容，确保上下文的连贯性。\r\n\r\nQ4: RAG技术是如何工作的？\r\nA4: RAG技术包括三个步骤：检索（从知识库中检索相关文档块）、增强（将检索到的内容作为上下文）、生成（调用大语言模型生成答案）。这样确保了答案的准确性和可控性。\r\n\r\nQ5: 如何提高问答的准确性？\r\nA5: 提高问答准确性的方法包括：1. 上传高质量的知识库文档；2. 确保文档内容准确、完整；3. 使用清晰的问题描述；4. 系统会自动检索最相关的文档块作为上下文。\r\n\r\nQ6: 系统支持哪些大语言模型？\r\nA6: 系统目前支持腾讯云混元大模型API，包括hunyuan-lite、hunyuan-standard、hunyuan-pro等模型。系统也预留了OpenAI API的接口，可以根据配置切换不同的LLM提供商。\r\n\r\nQ7: 对话历史会保存多久？\r\nA7: 对话历史会永久保存在数据库中，用户可以随时查看历史对话。系统使用软删除机制，删除的会话不会真正从数据库中删除，只是标记为已删除状态。\r\n\r\nQ8: 知识库文档有大小限制吗？\r\nA8: 理论上没有严格的文档大小限制，但建议单个文档不超过10MB。系统会自动将长文档分割成小块，每个块大约1000个字符，确保处理效率。\r\n\r\n【技术参数】\r\n\r\n1. 对话上下文长度\r\n系统默认保留最近30条消息（约15轮对话）作为上下文。这个长度可以根据需要调整，在application.yml配置文件中可以修改max-history参数。\r\n\r\n2. 文档分片大小\r\n默认文档块大小为1000个字符，块之间重叠大小为200个字符。这个参数可以根据实际需求调整，在TextChunkUtil工具类中可以修改。\r\n\r\n3. 检索返回数量\r\n默认检索返回数量为5个最相关的文档块。用户可以在检索请求中指定topK参数，建议设置在3-10之间，以获得最佳效果。\r\n\r\n4. API响应超时\r\nLLM API调用的超时时间默认设置为30秒。如果网络较慢或文档较大，可以适当增加超时时间。\r\n\r\n【注意事项】\r\n\r\n1. 知识库内容质量\r\n知识库内容的质量直接影响问答的准确性。建议上传准确、完整、结构清晰的文档。文档内容应该覆盖用户可能问到的问题。\r\n\r\n2. 问题描述清晰\r\n用户在提问时，应该尽量清晰地描述问题。过于模糊的问题可能导致系统无法准确理解，影响答案质量。\r\n\r\n3. 上下文管理\r\n系统会自动管理对话上下文，但用户也应该注意对话的连贯性。如果需要切换话题，建议创建新的对话会话。\r\n\r\n4. 知识库更新\r\n如果产品功能或使用方法发生变化，应该及时更新知识库内容。系统支持文档的增删改查，用户可以随时更新知识库。\r\n\r\n5. 隐私和安全\r\n所有对话记录和知识库内容都存储在数据库中，用户应该注意保护隐私信息。系统使用JWT认证，确保接口安全性。\r\n\r\n【最佳实践】\r\n\r\n1. 知识库构建\r\n建议按照产品功能模块组织知识库内容，每个模块一个文档。文档内容应该包括产品介绍、功能说明、使用方法、常见问题等。\r\n\r\n2. 对话管理\r\n建议为不同主题创建不同的对话会话，这样可以更好地管理对话历史。会话标题应该清晰描述对话主题。\r\n\r\n3. 问题优化\r\n用户在提问时，应该尽量使用完整的句子，避免使用过于简短的词语。系统能够理解自然语言，但清晰的问题描述有助于获得更好的答案。\r\n\r\n4. 答案验证\r\n系统返回的答案会附带参考来源，用户可以查看参考来源验证答案的准确性。如果答案不准确，可以检查知识库内容是否完整。\r\n\r\n【故障排除】\r\n\r\n1. 无法生成回复\r\n如果系统无法生成回复，可能的原因包括：API密钥配置错误、网络连接问题、API服务未开通等。请检查配置文件和网络连接。\r\n\r\n2. 检索结果为空\r\n如果检索结果为空，可能的原因包括：知识库中没有相关内容、查询关键词不匹配等。建议使用更宽泛的关键词或检查知识库内容。\r\n\r\n3. 答案不准确\r\n如果答案不准确，可能的原因包括：知识库内容不完整、检索到的文档块不相关、问题描述不清晰等。建议更新知识库内容或优化问题描述。\r\n\r\n4. 对话上下文丢失\r\n如果对话上下文丢失，可能的原因包括：会话被删除、消息记录被清除等。系统会自动保存所有对话记录，用户应该检查会话状态。', 'a302ce1fd54c00b7e94c1c5cf16ba37c89e0de46f11f2721bf8df5672fea91f0', 1, 41, NULL, '2025-11-06 07:22:59', '2025-11-06 07:22:59', 0);

-- --------------------------------------------------------

--
-- 表的结构 `operation_logs`
--

CREATE TABLE `operation_logs` (
  `id` bigint NOT NULL COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `operation` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `resource_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源ID',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '操作描述',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '用户代理',
  `request_data` json DEFAULT NULL COMMENT '请求数据',
  `response_data` json DEFAULT NULL COMMENT '响应数据',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作状态：success, failure, error',
  `error_message` text COLLATE utf8mb4_unicode_ci COMMENT '错误信息',
  `duration` int DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- --------------------------------------------------------

--
-- 表的结构 `permissions`
--

CREATE TABLE `permissions` (
  `id` bigint NOT NULL COMMENT '权限ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '权限描述',
  `module` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属模块',
  `resource` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '资源标识',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型：create, read, update, delete, execute',
  `is_system` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统权限：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

--
-- 转存表中的数据 `permissions`
--

INSERT INTO `permissions` (`id`, `name`, `code`, `description`, `module`, `resource`, `action`, `is_system`, `status`, `sort_order`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, '查看用户列表', 'user:list', '查看系统用户列表', 'user', 'user', 'read', 1, 1, 1, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(2, '创建用户', 'user:create', '创建新用户', 'user', 'user', 'create', 1, 1, 2, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(3, '编辑用户', 'user:update', '编辑用户信息', 'user', 'user', 'update', 1, 1, 3, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(4, '删除用户', 'user:delete', '删除用户', 'user', 'user', 'delete', 1, 1, 4, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(5, '重置用户密码', 'user:reset_password', '重置用户密码', 'user', 'user', 'execute', 1, 1, 5, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(6, '查看角色列表', 'role:list', '查看角色列表', 'role', 'role', 'read', 1, 1, 10, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(7, '创建角色', 'role:create', '创建新角色', 'role', 'role', 'create', 1, 1, 11, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(8, '编辑角色', 'role:update', '编辑角色信息', 'role', 'role', 'update', 1, 1, 12, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(9, '删除角色', 'role:delete', '删除角色', 'role', 'role', 'delete', 1, 1, 13, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(10, '分配角色权限', 'role:assign_permission', '为角色分配权限', 'role', 'role', 'execute', 1, 1, 14, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(11, '查看权限列表', 'permission:list', '查看权限列表', 'permission', 'permission', 'read', 1, 1, 20, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(12, '分配用户角色', 'user:assign_role', '为用户分配角色', 'user', 'user', 'execute', 1, 1, 21, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(13, '查看团队列表', 'team:list', '查看团队列表', 'team', 'team', 'read', 1, 1, 30, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(14, '创建团队', 'team:create', '创建新团队', 'team', 'team', 'create', 1, 1, 31, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(15, '编辑团队', 'team:update', '编辑团队信息', 'team', 'team', 'update', 1, 1, 32, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(16, '删除团队', 'team:delete', '删除团队', 'team', 'team', 'delete', 1, 1, 33, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(17, '管理团队成员', 'team:manage_member', '管理团队成员', 'team', 'team_member', 'execute', 1, 1, 34, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(18, '邀请团队成员', 'team:invite', '邀请用户加入团队', 'team', 'team_invitation', 'create', 1, 1, 35, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(19, '审批加入申请', 'team:approve_request', '审批团队加入申请', 'team', 'team_request', 'execute', 1, 1, 36, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(20, '查看系统配置', 'system:config:read', '查看系统配置', 'system', 'config', 'read', 1, 1, 40, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(21, '修改系统配置', 'system:config:write', '修改系统配置', 'system', 'config', 'update', 1, 1, 41, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(22, '查看操作日志', 'system:log:read', '查看系统操作日志', 'system', 'log', 'read', 1, 1, 42, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL),
(23, '查看登录日志', 'system:login_log:read', '查看用户登录日志', 'system', 'login_log', 'read', 1, 1, 43, '2025-10-23 06:40:22', '2025-10-23 06:40:22', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `privacy_data`
--

CREATE TABLE `privacy_data` (
  `id` bigint NOT NULL COMMENT '数据ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint DEFAULT NULL COMMENT '会话ID',
  `data_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据类型：phone-手机号, email-邮箱, idcard-身份证, bankcard-银行卡, address-地址等',
  `data_value` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据值（加密存储）',
  `data_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据哈希值（用于去重和检索）',
  `source` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据来源：user_input-用户输入, ai_response-AI回复, system-系统',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作类型：detected-检测到, stored-已存储, deleted-已删除, masked-已脱敏',
  `retention_days` int DEFAULT NULL COMMENT '保留天数（数据最小化策略）',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间（自动删除时间）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='隐私数据表';

--
-- 转存表中的数据 `privacy_data`
--

INSERT INTO `privacy_data` (`id`, `user_id`, `session_id`, `data_type`, `data_value`, `data_hash`, `source`, `action`, `retention_days`, `expires_at`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 12, 2, 'phone', '17825648412', '0500a05a02d04bc332e8cfdcdc5d9153faeeb45661149b6c8ce8674110e65b31', 'user_input', 'detected', 30, '2025-12-06 18:23:51', '2025-11-06 10:23:51', '2025-11-06 10:23:51', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `risk_records`
--

CREATE TABLE `risk_records` (
  `id` bigint NOT NULL COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` bigint DEFAULT NULL COMMENT '会话ID',
  `message_id` bigint DEFAULT NULL COMMENT '消息ID',
  `risk_type` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '风险类型：sensitive-敏感词, crisis-危机, privacy-隐私泄露',
  `risk_level` tinyint NOT NULL COMMENT '风险级别：1-低，2-中，3-高，4-严重',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '触发风险的内容',
  `matched_rule` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '匹配的规则（规则名称或敏感词）',
  `action` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '处理动作：blocked-已阻止, warned-已警告, alerted-已告警, reviewed-已审核',
  `details` json DEFAULT NULL COMMENT '详细信息（JSON格式）',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '用户代理',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风险记录表';

--
-- 转存表中的数据 `risk_records`
--

INSERT INTO `risk_records` (`id`, `user_id`, `session_id`, `message_id`, `risk_type`, `risk_level`, `content`, `matched_rule`, `action`, `details`, `ip_address`, `user_agent`, `created_at`) VALUES
(1, 12, 2, NULL, 'sensitive', 2, '讨论一下国内政治', '', 'warn', '{\"riskType\": \"sensitive\", \"riskLevel\": 2, \"timestamp\": \"2025-11-06T18:21:27.031628500\"}', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 SLBrowser/9.0.6.8151 SLBChan/115 SLBVPV/64-bit', '2025-11-06 10:21:27'),
(2, 12, 2, NULL, 'crisis', 3, '17825648412这是我的手机号', '隐私信息泄露', 'warn', '{\"riskType\": \"crisis\", \"riskLevel\": 3, \"timestamp\": \"2025-11-06T18:23:51.044615900\"}', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 SLBrowser/9.0.6.8151 SLBChan/115 SLBVPV/64-bit', '2025-11-06 10:23:51');

-- --------------------------------------------------------

--
-- 表的结构 `roles`
--

CREATE TABLE `roles` (
  `id` bigint NOT NULL COMMENT '角色ID',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '角色描述',
  `is_system` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统角色：0-否，1-是（系统角色不可删除）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序顺序',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

--
-- 转存表中的数据 `roles`
--

INSERT INTO `roles` (`id`, `name`, `code`, `description`, `is_system`, `status`, `sort_order`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, '超级管理员', 'super_admin', '系统最高权限管理员，拥有所有权限', 1, 1, 1, '2025-10-23 06:40:13', '2025-10-23 06:40:13', NULL),
(2, '系统管理员', 'admin', '系统管理员，可管理用户、角色、团队等', 1, 1, 2, '2025-10-23 06:40:13', '2025-10-23 06:40:13', NULL),
(3, '团队负责人', 'team_owner', '团队创建者和最高管理者', 1, 1, 3, '2025-10-23 06:40:13', '2025-10-23 06:40:13', NULL),
(4, '团队管理员', 'team_admin', '团队管理员，可管理团队成员', 1, 1, 4, '2025-10-23 06:40:13', '2025-10-23 06:40:13', NULL),
(5, '普通用户', 'user', '普通注册用户', 1, 1, 5, '2025-10-23 06:40:13', '2025-10-23 06:40:13', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `role_permissions`
--

CREATE TABLE `role_permissions` (
  `id` bigint NOT NULL COMMENT '关联ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

--
-- 转存表中的数据 `role_permissions`
--

INSERT INTO `role_permissions` (`id`, `role_id`, `permission_id`, `created_at`) VALUES
(1, 1, 1, '2025-10-23 06:40:32'),
(2, 1, 2, '2025-10-23 06:40:32'),
(3, 1, 3, '2025-10-23 06:40:32'),
(4, 1, 4, '2025-10-23 06:40:32'),
(5, 1, 5, '2025-10-23 06:40:32'),
(6, 1, 6, '2025-10-23 06:40:32'),
(7, 1, 7, '2025-10-23 06:40:32'),
(8, 1, 8, '2025-10-23 06:40:32'),
(9, 1, 9, '2025-10-23 06:40:32'),
(10, 1, 10, '2025-10-23 06:40:32'),
(11, 1, 11, '2025-10-23 06:40:32'),
(12, 1, 12, '2025-10-23 06:40:32'),
(13, 1, 13, '2025-10-23 06:40:32'),
(14, 1, 14, '2025-10-23 06:40:32'),
(15, 1, 15, '2025-10-23 06:40:32'),
(16, 1, 16, '2025-10-23 06:40:32'),
(17, 1, 17, '2025-10-23 06:40:32'),
(18, 1, 18, '2025-10-23 06:40:32'),
(19, 1, 19, '2025-10-23 06:40:32'),
(20, 1, 20, '2025-10-23 06:40:32'),
(21, 1, 21, '2025-10-23 06:40:32'),
(22, 1, 22, '2025-10-23 06:40:32'),
(23, 1, 23, '2025-10-23 06:40:32'),
(32, 2, 11, '2025-10-23 06:40:32'),
(33, 2, 10, '2025-10-23 06:40:32'),
(34, 2, 7, '2025-10-23 06:40:32'),
(35, 2, 6, '2025-10-23 06:40:32'),
(36, 2, 8, '2025-10-23 06:40:32'),
(37, 2, 20, '2025-10-23 06:40:32'),
(38, 2, 22, '2025-10-23 06:40:32'),
(39, 2, 23, '2025-10-23 06:40:32'),
(40, 2, 16, '2025-10-23 06:40:32'),
(41, 2, 13, '2025-10-23 06:40:32'),
(42, 2, 17, '2025-10-23 06:40:32'),
(43, 2, 15, '2025-10-23 06:40:32'),
(44, 2, 12, '2025-10-23 06:40:32'),
(45, 2, 2, '2025-10-23 06:40:32'),
(46, 2, 1, '2025-10-23 06:40:32'),
(47, 2, 5, '2025-10-23 06:40:32'),
(48, 2, 3, '2025-10-23 06:40:32'),
(63, 3, 19, '2025-10-23 06:40:33'),
(64, 3, 14, '2025-10-23 06:40:33'),
(65, 3, 16, '2025-10-23 06:40:33'),
(66, 3, 18, '2025-10-23 06:40:33'),
(67, 3, 17, '2025-10-23 06:40:33'),
(68, 3, 15, '2025-10-23 06:40:33'),
(70, 4, 19, '2025-10-23 06:40:33'),
(71, 4, 18, '2025-10-23 06:40:33'),
(72, 4, 17, '2025-10-23 06:40:33'),
(73, 4, 15, '2025-10-23 06:40:33'),
(77, 5, 14, '2025-10-23 06:40:33');

-- --------------------------------------------------------

--
-- 表的结构 `security_rules`
--

CREATE TABLE `security_rules` (
  `id` bigint NOT NULL COMMENT '规则ID',
  `rule_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `rule_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则类型：keyword-关键词, pattern-正则模式, ai-AI检测',
  `pattern` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '匹配模式（正则表达式或关键词）',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：crisis-危机, risk-风险, compliance-合规',
  `risk_level` tinyint NOT NULL DEFAULT '1' COMMENT '风险级别：1-低，2-中，3-高，4-严重',
  `action` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'warn' COMMENT '处理动作：block-阻止, warn-警告, alert-告警, review-人工审核',
  `warning_message` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '警告消息',
  `guidance` text COLLATE utf8mb4_unicode_ci COMMENT '话术指导（建议的回复方式）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级（数字越大优先级越高）',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全规则表';

--
-- 转存表中的数据 `security_rules`
--

INSERT INTO `security_rules` (`id`, `rule_name`, `rule_type`, `pattern`, `category`, `risk_level`, `action`, `warning_message`, `guidance`, `status`, `priority`, `description`, `created_at`, `updated_at`) VALUES
(1, '自杀倾向检测', 'keyword', '自杀|自残|不想活了', 'crisis', 4, 'alert', '检测到高风险内容，请及时关注用户心理健康', '建议引导用户寻求专业帮助，提供心理咨询热线', 1, 100, '检测用户可能的自杀倾向', '2025-11-06 10:20:07', '2025-11-06 10:20:07'),
(2, '暴力威胁检测', 'pattern', '(杀|伤害|攻击).*(你|他|她|他们)', 'crisis', 4, 'alert', '检测到暴力威胁内容', '建议立即阻止并记录，必要时报警', 1, 90, '检测暴力威胁', '2025-11-06 10:20:07', '2025-11-06 10:20:07'),
(3, '隐私信息泄露', 'pattern', '\\d{17}[\\dXx]|1[3-9]\\d{9}', 'privacy', 3, 'warn', '检测到可能包含隐私信息', '建议提醒用户注意隐私保护', 1, 50, '检测身份证号、手机号等隐私信息', '2025-11-06 10:20:07', '2025-11-06 10:20:07');

-- --------------------------------------------------------

--
-- 表的结构 `sensitive_words`
--

CREATE TABLE `sensitive_words` (
  `id` bigint NOT NULL COMMENT '敏感词ID',
  `word` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '敏感词',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类：violence-暴力, porn-色情, politics-政治, fraud-诈骗, privacy-隐私等',
  `level` tinyint NOT NULL DEFAULT '1' COMMENT '风险级别：1-低风险，2-中风险，3-高风险',
  `action` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'warn' COMMENT '处理动作：block-阻止, warn-警告, replace-替换',
  `replacement` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '替换词（当action为replace时使用）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词表';

--
-- 转存表中的数据 `sensitive_words`
--

INSERT INTO `sensitive_words` (`id`, `word`, `category`, `level`, `action`, `replacement`, `status`, `description`, `created_at`, `updated_at`) VALUES
(1, '暴力', 'violence', 3, 'block', NULL, 1, '暴力相关敏感词', '2025-11-06 10:20:07', '2025-11-06 10:20:07'),
(2, '色情', 'porn', 3, 'block', NULL, 1, '色情相关敏感词', '2025-11-06 10:20:07', '2025-11-06 10:20:07'),
(3, '政治', 'politics', 2, 'warn', NULL, 1, '政治相关敏感词', '2025-11-06 10:20:07', '2025-11-06 10:20:07'),
(4, '诈骗', 'fraud', 3, 'block', NULL, 1, '诈骗相关敏感词', '2025-11-06 10:20:07', '2025-11-06 10:20:07');

-- --------------------------------------------------------

--
-- 表的结构 `system_configs`
--

CREATE TABLE `system_configs` (
  `id` bigint NOT NULL COMMENT '配置ID',
  `config_key` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text COLLATE utf8mb4_unicode_ci COMMENT '配置值',
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '配置描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'string' COMMENT '值类型：string, number, boolean, json',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开：0-否，1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

--
-- 转存表中的数据 `system_configs`
--

INSERT INTO `system_configs` (`id`, `config_key`, `config_value`, `description`, `type`, `is_public`, `created_at`, `updated_at`) VALUES
(1, 'site_name', '用户管理系统', '网站名称', 'string', 1, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(2, 'site_description', '基于RBAC的用户管理系统', '网站描述', 'string', 1, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(3, 'user_register_enabled', 'true', '是否允许用户注册', 'boolean', 1, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(4, 'email_verification_required', 'true', '注册时是否需要邮箱验证', 'boolean', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(5, 'login_max_attempts', '5', '登录最大失败次数', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(6, 'login_lock_duration', '30', '登录锁定时长（分钟）', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(7, 'verification_code_expires', '5', '验证码有效期（分钟）', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(8, 'invitation_expires_days', '7', '邀请链接有效期（天）', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(9, 'max_teams_per_user', '10', '每个用户最大创建团队数', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42'),
(10, 'default_team_max_members', '50', '团队默认最大成员数', 'number', 0, '2025-10-23 06:40:42', '2025-10-23 06:40:42');

-- --------------------------------------------------------

--
-- 表的结构 `teams`
--

CREATE TABLE `teams` (
  `id` bigint NOT NULL COMMENT '团队ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团队名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团队编码',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '团队描述',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '团队头像URL',
  `visibility` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'private' COMMENT '可见性：public-公开，private-私有',
  `join_policy` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'approval' COMMENT '加入策略：open-开放，approval-需审批，invite-仅邀请',
  `max_members` int DEFAULT NULL COMMENT '最大成员数（null表示无限制）',
  `owner_id` bigint NOT NULL COMMENT '团队负责人ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常，2-已解散',
  `settings` json DEFAULT NULL COMMENT '团队设置（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队表';

--
-- 转存表中的数据 `teams`
--

INSERT INTO `teams` (`id`, `name`, `code`, `description`, `avatar_url`, `visibility`, `join_policy`, `max_members`, `owner_id`, `status`, `settings`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, '测试团队', 'TEAM_DF05B881', '这是一个测试团队', NULL, 'private', 'approval', NULL, 7, 1, NULL, '2025-10-28 08:24:16', '2025-10-28 08:24:16', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `team_invitations`
--

CREATE TABLE `team_invitations` (
  `id` bigint NOT NULL COMMENT '邀请ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `inviter_id` bigint NOT NULL COMMENT '邀请者ID',
  `invitee_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '被邀请者邮箱',
  `invitee_user_id` bigint DEFAULT NULL COMMENT '被邀请者用户ID（如果已注册）',
  `invitation_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邀请码',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'member' COMMENT '邀请角色',
  `message` text COLLATE utf8mb4_unicode_ci COMMENT '邀请消息',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待处理，accepted-已接受，rejected-已拒绝，expired-已过期',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `responded_at` datetime DEFAULT NULL COMMENT '响应时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队邀请表';

-- --------------------------------------------------------

--
-- 表的结构 `team_join_requests`
--

CREATE TABLE `team_join_requests` (
  `id` bigint NOT NULL COMMENT '申请ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '申请者ID',
  `message` text COLLATE utf8mb4_unicode_ci COMMENT '申请消息',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '状态：pending-待审批，approved-已批准，rejected-已拒绝，cancelled-已取消',
  `reviewed_by` bigint DEFAULT NULL COMMENT '审批者ID',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审批时间',
  `review_message` text COLLATE utf8mb4_unicode_ci COMMENT '审批意见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队加入申请表';

-- --------------------------------------------------------

--
-- 表的结构 `team_members`
--

CREATE TABLE `team_members` (
  `id` bigint NOT NULL COMMENT '成员ID',
  `team_id` bigint NOT NULL COMMENT '团队ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'member' COMMENT '团队角色：owner-负责人，admin-管理员，member-普通成员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-已移除，1-正常',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `invited_by` bigint DEFAULT NULL COMMENT '邀请者ID',
  `permissions` json DEFAULT NULL COMMENT '特殊权限设置（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团队成员表';

--
-- 转存表中的数据 `team_members`
--

INSERT INTO `team_members` (`id`, `team_id`, `user_id`, `role`, `status`, `joined_at`, `invited_by`, `permissions`, `created_at`, `updated_at`) VALUES
(1, 1, 7, 'owner', 1, '2025-10-28 08:24:16', 7, NULL, '2025-10-28 08:24:16', '2025-10-28 08:24:16');

-- --------------------------------------------------------

--
-- 表的结构 `t_app`
--

CREATE TABLE `t_app` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '应用名称',
  `description` varchar(255) NOT NULL COMMENT '简短描述',
  `full_description` text COMMENT '详细描述',
  `avatar` varchar(255) DEFAULT NULL COMMENT '应用头像URL',
  `category` varchar(64) DEFAULT NULL COMMENT '应用分类',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '价格',
  `rating` double DEFAULT NULL COMMENT '评分',
  `downloads` int DEFAULT NULL COMMENT '下载量',
  `reviews` int DEFAULT NULL COMMENT '评论数',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `published_at` date DEFAULT NULL COMMENT '发布日期',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用信息表';

--
-- 转存表中的数据 `t_app`
--

INSERT INTO `t_app` (`id`, `name`, `description`, `full_description`, `avatar`, `category`, `price`, `rating`, `downloads`, `reviews`, `author`, `published_at`, `created_at`) VALUES
(1, '智能客服助手', '基于大语言模型的智能客服系统，支持多轮对话和知识库检索', '这是一个功能强大的智能客服系统，能够理解用户意图，提供准确回答。支持多轮对话、知识库检索、情感分析等功能。', 'https://via.placeholder.com/80x80', 'customer-service', 0.00, 4.8, 1200, 156, 'AI团队', '2024-01-15', '2025-09-25 08:49:41'),
(2, '内容创作大师MySQL', 'AI驱动的内容生成和优化工具，支持多种文体创作', '专业的内容创作工具，利用AI技术帮助用户快速生成高质量内容。支持文章、广告文案、社交媒体内容等多种文体的创作和优化。', 'https://via.placeholder.com/80x80', 'content-creation', 0.00, 4.9, 980, 123, '创作工坊', '2024-01-10', '2025-09-25 08:49:41');

-- --------------------------------------------------------

--
-- 表的结构 `users`
--

CREATE TABLE `users` (
  `id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱地址',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希值',
  `nickname` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `bio` text COLLATE utf8mb4_unicode_ci COMMENT '个人简介',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账户状态：0-禁用，1-正常，2-锁定',
  `email_verified` tinyint NOT NULL DEFAULT '0' COMMENT '邮箱是否验证：0-未验证，1-已验证',
  `phone_verified` tinyint NOT NULL DEFAULT '0' COMMENT '手机是否验证：0-未验证，1-已验证',
  `login_attempts` int NOT NULL DEFAULT '0' COMMENT '登录失败次数',
  `locked_until` datetime DEFAULT NULL COMMENT '锁定到期时间',
  `last_login_at` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime DEFAULT NULL COMMENT '删除时间（软删除）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

--
-- 转存表中的数据 `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `phone`, `password_hash`, `nickname`, `avatar_url`, `bio`, `status`, `email_verified`, `phone_verified`, `login_attempts`, `locked_until`, `last_login_at`, `last_login_ip`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 'system', 'system@example.com', NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统用户', NULL, NULL, 1, 1, 0, 2, NULL, NULL, NULL, '2025-11-06 07:22:59', '2025-11-06 07:29:46', NULL),
(2, 'hth123', '1749683268@qq.com', '15563718629', '$2a$10$eXWsoHYcGCNLnCLbsDYvB.uPn1zE2yzGTeILgr4W/5MdIB6wa6j2y', 'hth', NULL, NULL, 1, 0, 0, 0, NULL, '2025-11-06 18:01:10', '127.0.0.1', '2025-10-27 05:41:52', '2025-11-06 10:01:09', NULL),
(7, 'testuser', 'test@example.com', NULL, '$2a$10$wVE3D.FD0PqjaWlXmDKJm.kuZ4DEZbuOyFeQOQ.Se4FMI87NudTsS', '测试用户', NULL, NULL, 1, 0, 0, 0, NULL, '2025-11-01 17:09:50', '127.0.0.1', '2025-10-28 08:23:11', '2025-11-01 09:09:50', NULL),
(12, 'test1', '123@qq.com', NULL, '$2a$10$wuDE.ILVCOf5.oaPp7k6L.tT1FFW8G2VVBvZuNsGEhaxb4UHpFlEi', NULL, NULL, NULL, 1, 0, 0, 0, NULL, '2025-11-06 19:12:07', '127.0.0.1', '2025-11-05 11:30:54', '2025-11-06 11:12:06', NULL),
(13, 'admin', 'admin@example.com', NULL, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', NULL, NULL, 1, 1, 0, 5, '2025-11-06 19:33:10', NULL, NULL, '2025-11-06 07:51:30', '2025-11-06 11:03:09', NULL),
(14, 'test2', NULL, '15563715824', '$2a$10$Dn/YzUy9/t.Xma8TMIqDJOWS/EZbzitzwGpjy9Dwi7hpb8zlabIVm', NULL, NULL, NULL, 1, 0, 1, 0, NULL, '2025-11-06 19:02:18', '127.0.0.1', '2025-11-06 11:02:07', '2025-11-06 11:02:17', NULL),
(15, 'test3', '12384@qq.com', NULL, '$2a$10$SFaFbG5w2WqJ7Ynlh/AWBeKdiOFXViTnNHu/ZNVqzqMqzVtXauHJa', NULL, NULL, NULL, 1, 1, 0, 0, NULL, NULL, NULL, '2025-11-06 11:11:55', '2025-11-06 11:11:55', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `user_login_logs`
--

CREATE TABLE `user_login_logs` (
  `id` bigint NOT NULL COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `login_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录方式：email, username, phone, sms',
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录IP地址',
  `user_agent` text COLLATE utf8mb4_unicode_ci COMMENT '用户代理信息',
  `device_info` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '设备信息',
  `location` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '登录地点',
  `status` tinyint NOT NULL COMMENT '登录状态：0-失败，1-成功',
  `failure_reason` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '失败原因',
  `session_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

--
-- 转存表中的数据 `user_login_logs`
--

INSERT INTO `user_login_logs` (`id`, `user_id`, `login_type`, `ip_address`, `user_agent`, `device_info`, `location`, `status`, `failure_reason`, `session_id`, `created_at`) VALUES
(1, 7, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '7edd23af0f3b4019', '2025-10-28 08:23:12'),
(2, 7, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '6cb36da16714474b', '2025-10-28 08:27:20'),
(3, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'b41e028591404d83', '2025-10-29 10:37:59'),
(4, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'f3dcfe0b4b2547e4', '2025-10-29 10:38:22'),
(5, 2, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-10-29 10:52:07'),
(6, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'fbca428f77ae4799', '2025-10-29 11:06:09'),
(7, 2, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-10-29 11:08:54'),
(8, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '482ced93d0a147f7', '2025-10-29 11:09:04'),
(9, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '9b2864a5722e4aff', '2025-10-29 11:10:32'),
(10, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'd977aff1c6464a26', '2025-10-29 11:20:26'),
(11, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'efead461c706403f', '2025-10-29 11:22:29'),
(12, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '8c2837fad4a34a70', '2025-10-29 11:44:42'),
(13, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '2ab2882f32924950', '2025-10-29 11:55:37'),
(14, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '1ae52536cf2f40b6', '2025-10-29 12:15:01'),
(15, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '8abd8e09f3674c32', '2025-10-29 12:28:06'),
(16, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'e19688d340344e38', '2025-10-29 12:32:00'),
(17, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '0750fa76a252408e', '2025-11-01 07:53:00'),
(18, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '26b346ea5df644ff', '2025-11-01 08:10:25'),
(19, 7, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '849aaad5b1624bd0', '2025-11-01 08:29:42'),
(20, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '951359948aa24f2a', '2025-11-01 08:43:18'),
(21, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'aaec829b403a48ee', '2025-11-01 08:49:46'),
(22, 7, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'd2c7993c689844fd', '2025-11-01 09:09:50'),
(23, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '3b9204b60bdb4dad', '2025-11-01 09:17:36'),
(24, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'd3411a59746340da', '2025-11-05 10:11:07'),
(25, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'ca030997f2d54888', '2025-11-05 10:20:20'),
(27, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '06e6604ced9b480e', '2025-11-05 11:11:03'),
(29, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '49e7868556344588', '2025-11-05 11:18:53'),
(33, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'e57c1d9016c64d86', '2025-11-05 11:31:01'),
(34, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '0dc0cafd69934e1c', '2025-11-05 12:29:32'),
(35, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '0307fd7034954fb9', '2025-11-06 07:28:32'),
(36, 1, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 07:28:55'),
(37, 1, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 07:29:46'),
(38, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'e737732ce0cb4610', '2025-11-06 07:30:04'),
(39, 13, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 07:53:21'),
(40, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '31a9ffc7c4e9449d', '2025-11-06 07:53:36'),
(41, 2, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '33d2f0c3818d4c2e', '2025-11-06 10:01:09'),
(42, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, 'a567734268944135', '2025-11-06 10:01:46'),
(43, 13, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 10:02:42'),
(44, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '78675ccffb79486f', '2025-11-06 10:02:50'),
(45, 14, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '3b7c515fb7494e27', '2025-11-06 11:02:17'),
(46, 13, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 11:02:58'),
(47, 13, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 11:03:06'),
(48, 13, 'username', '127.0.0.1', 'Web Browser', NULL, NULL, 0, '密码错误', NULL, '2025-11-06 11:03:09'),
(49, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '670efc4bb4a64d44', '2025-11-06 11:04:03'),
(50, 12, 'username', '127.0.0.1', 'Web Browser', 'PC', '本地', 1, NULL, '5dc85bfc64374c31', '2025-11-06 11:12:06');

-- --------------------------------------------------------

--
-- 表的结构 `user_roles`
--

CREATE TABLE `user_roles` (
  `id` bigint NOT NULL COMMENT '关联ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `assigned_by` bigint DEFAULT NULL COMMENT '分配者用户ID',
  `assigned_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间（可选）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

--
-- 转存表中的数据 `user_roles`
--

INSERT INTO `user_roles` (`id`, `user_id`, `role_id`, `assigned_by`, `assigned_at`, `expires_at`, `created_at`) VALUES
(1, 12, 5, 12, '2025-11-05 11:30:54', NULL, '2025-11-05 11:30:54'),
(2, 14, 5, 14, '2025-11-06 11:02:07', NULL, '2025-11-06 11:02:07'),
(3, 15, 5, 15, '2025-11-06 11:11:55', NULL, '2025-11-06 11:11:55');

-- --------------------------------------------------------

--
-- 表的结构 `user_verification_codes`
--

CREATE TABLE `user_verification_codes` (
  `id` bigint NOT NULL COMMENT '验证码ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（注册时可能为空）',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码类型：email_register, email_reset, sms_register, sms_reset, sms_login',
  `target` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标邮箱或手机号',
  `code` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `used` tinyint NOT NULL DEFAULT '0' COMMENT '是否已使用：0-未使用，1-已使用',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户验证码表';

--
-- 转存表中的数据 `user_verification_codes`
--

INSERT INTO `user_verification_codes` (`id`, `user_id`, `type`, `target`, `code`, `used`, `expires_at`, `created_at`) VALUES
(1, NULL, 'email_register', '12345@qq.com', '065779', 0, '2025-11-06 18:46:31', '2025-11-06 10:41:30'),
(2, NULL, 'email_register', '1749683268@qq.com', '480907', 0, '2025-11-06 18:46:44', '2025-11-06 10:41:44'),
(3, NULL, 'email_register', '1749683268@qq.com', '196059', 0, '2025-11-06 18:47:03', '2025-11-06 10:42:02'),
(4, NULL, 'sms_register', '15563718629', '213671', 0, '2025-11-06 18:47:22', '2025-11-06 10:42:21'),
(5, NULL, 'sms_register', '15563718412', '816904', 0, '2025-11-06 18:49:12', '2025-11-06 10:44:12'),
(6, NULL, 'email_register', '1749683268@qq.com', '810080', 0, '2025-11-06 18:52:45', '2025-11-06 10:47:45'),
(7, NULL, 'email_register', '12345@qq.com', '152374', 0, '2025-11-06 18:58:58', '2025-11-06 10:53:58'),
(8, NULL, 'email_register', '1749683268@qq.com', '665527', 0, '2025-11-06 18:59:14', '2025-11-06 10:54:14'),
(9, NULL, 'sms_register', '15563714423', '643855', 1, '2025-11-06 18:59:28', '2025-11-06 10:54:27'),
(10, NULL, 'email_register', '12345@qq.com', '853129', 0, '2025-11-06 19:03:28', '2025-11-06 10:58:28'),
(11, NULL, 'email_register', '12345@qq.com', '496594', 0, '2025-11-06 19:03:55', '2025-11-06 10:58:54'),
(12, NULL, 'sms_register', '15563715824', '777926', 1, '2025-11-06 19:06:48', '2025-11-06 11:01:48'),
(13, NULL, 'email_register', '12384@qq.com', '858283', 1, '2025-11-06 19:16:24', '2025-11-06 11:11:24');

--
-- 转储表的索引
--

--
-- 表的索引 `chat_message`
--
ALTER TABLE `chat_message`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_session` (`session_id`);

--
-- 表的索引 `agents`
--
ALTER TABLE `agents`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `ux_agents_name` (`name`),
  ADD KEY `idx_agents_status` (`status`),
  ADD KEY `idx_agents_user_id` (`user_id`),
  ADD KEY `idx_agents_updated_at` (`updated_at`);

--
-- 表的索引 `workflows`
--
ALTER TABLE `workflows`
  ADD KEY `idx_workflows_user_id` (`user_id`),
  ADD KEY `idx_workflows_updated_at` (`updated_at`);

--
-- 表的索引 `workflow_nodes`
--
ALTER TABLE `workflow_nodes`
  ADD UNIQUE KEY `uk_workflow_nodes_workflow_seq` (`workflow_id`,`seq`),
  ADD KEY `idx_workflow_nodes_workflow_id` (`workflow_id`),
  ADD KEY `idx_workflow_nodes_agent_id` (`agent_id`);

--
-- 表的索引 `agent_messages`
--
ALTER TABLE `agent_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_agent_messages_session` (`session_id`),
  ADD KEY `idx_agent_messages_created_at` (`created_at`);

--
-- 表的索引 `agent_sessions`
--
ALTER TABLE `agent_sessions`
  ADD KEY `idx_agent_sessions_agent` (`agent_id`);

--
-- 表的索引 `chat_session`
--
ALTER TABLE `chat_session`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user` (`user_id`);

--
-- 表的索引 `knowledge_chunk`
--
ALTER TABLE `knowledge_chunk`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_document_id` (`document_id`),
  ADD KEY `idx_chunk_index` (`document_id`,`chunk_index`),
  ADD KEY `idx_content_hash` (`content_hash`);

--
-- 表的索引 `knowledge_chunk_embedding`
--
ALTER TABLE `knowledge_chunk_embedding`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_chunk_model` (`chunk_id`,`embedding_model`),
  ADD KEY `idx_chunk_id` (`chunk_id`);

--
-- 表的索引 `knowledge_document`
--
ALTER TABLE `knowledge_document`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_content_hash` (`content_hash`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- 表的索引 `operation_logs`
--
ALTER TABLE `operation_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_operation` (`operation`),
  ADD KEY `idx_resource` (`resource_type`,`resource_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_user_operation_time` (`user_id`,`operation`,`created_at`);

--
-- 表的索引 `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_code` (`code`),
  ADD KEY `idx_module` (`module`),
  ADD KEY `idx_resource_action` (`resource`,`action`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_deleted_at` (`deleted_at`);

--
-- 表的索引 `privacy_data`
--
ALTER TABLE `privacy_data`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_session_id` (`session_id`),
  ADD KEY `idx_data_type` (`data_type`),
  ADD KEY `idx_data_hash` (`data_hash`),
  ADD KEY `idx_expires_at` (`expires_at`),
  ADD KEY `idx_deleted_at` (`deleted_at`);

--
-- 表的索引 `risk_records`
--
ALTER TABLE `risk_records`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_session_id` (`session_id`),
  ADD KEY `idx_risk_type` (`risk_type`),
  ADD KEY `idx_risk_level` (`risk_level`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- 表的索引 `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_code` (`code`),
  ADD UNIQUE KEY `uk_name` (`name`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_sort_order` (`sort_order`),
  ADD KEY `idx_deleted_at` (`deleted_at`);

--
-- 表的索引 `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  ADD KEY `idx_role_id` (`role_id`),
  ADD KEY `idx_permission_id` (`permission_id`);

--
-- 表的索引 `security_rules`
--
ALTER TABLE `security_rules`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_rule_type` (`rule_type`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_risk_level` (`risk_level`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_priority` (`priority`);

--
-- 表的索引 `sensitive_words`
--
ALTER TABLE `sensitive_words`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_word` (`word`),
  ADD KEY `idx_category` (`category`),
  ADD KEY `idx_level` (`level`),
  ADD KEY `idx_status` (`status`);

--
-- 表的索引 `system_configs`
--
ALTER TABLE `system_configs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_config_key` (`config_key`),
  ADD KEY `idx_is_public` (`is_public`);

--
-- 表的索引 `teams`
--
ALTER TABLE `teams`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_name` (`name`),
  ADD UNIQUE KEY `uk_code` (`code`),
  ADD KEY `idx_owner_id` (`owner_id`),
  ADD KEY `idx_visibility` (`visibility`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_deleted_at` (`deleted_at`);

--
-- 表的索引 `team_invitations`
--
ALTER TABLE `team_invitations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_invitation_code` (`invitation_code`),
  ADD KEY `idx_team_id` (`team_id`),
  ADD KEY `idx_inviter_id` (`inviter_id`),
  ADD KEY `idx_invitee_email` (`invitee_email`),
  ADD KEY `idx_invitee_user_id` (`invitee_user_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_expires_at` (`expires_at`);

--
-- 表的索引 `team_join_requests`
--
ALTER TABLE `team_join_requests`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_team_user_pending` (`team_id`,`user_id`,`status`),
  ADD KEY `idx_team_id` (`team_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_reviewed_by` (`reviewed_by`),
  ADD KEY `idx_created_at` (`created_at`);

--
-- 表的索引 `team_members`
--
ALTER TABLE `team_members`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_team_user` (`team_id`,`user_id`),
  ADD KEY `idx_team_id` (`team_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_role` (`role`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_invited_by` (`invited_by`),
  ADD KEY `idx_team_role_status` (`team_id`,`role`,`status`);

--
-- 表的索引 `t_app`
--
ALTER TABLE `t_app`
  ADD PRIMARY KEY (`id`);

--
-- 表的索引 `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_username` (`username`),
  ADD UNIQUE KEY `uk_email` (`email`),
  ADD UNIQUE KEY `uk_phone` (`phone`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_deleted_at` (`deleted_at`),
  ADD KEY `idx_email_status` (`email`,`status`),
  ADD KEY `idx_phone_status` (`phone`,`status`);

--
-- 表的索引 `user_login_logs`
--
ALTER TABLE `user_login_logs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_ip_address` (`ip_address`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_status` (`status`),
  ADD KEY `idx_user_time` (`user_id`,`created_at`);

--
-- 表的索引 `user_roles`
--
ALTER TABLE `user_roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  ADD KEY `idx_user_id` (`user_id`),
  ADD KEY `idx_role_id` (`role_id`),
  ADD KEY `idx_assigned_by` (`assigned_by`),
  ADD KEY `idx_expires_at` (`expires_at`);

--
-- 表的索引 `user_verification_codes`
--
ALTER TABLE `user_verification_codes`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_target_type` (`target`,`type`),
  ADD KEY `idx_code` (`code`),
  ADD KEY `idx_expires_at` (`expires_at`),
  ADD KEY `idx_user_id` (`user_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `chat_message`
--
ALTER TABLE `chat_message`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID', AUTO_INCREMENT=60;

--
-- 使用表AUTO_INCREMENT `chat_session`
--
ALTER TABLE `chat_session`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID', AUTO_INCREMENT=4;

--
-- 使用表AUTO_INCREMENT `workflows`
--
ALTER TABLE `workflows`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作流ID';

--
-- 使用表AUTO_INCREMENT `workflow_nodes`
--
ALTER TABLE `workflow_nodes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工作流节点ID';

--
-- 使用表AUTO_INCREMENT `agent_messages`
--
ALTER TABLE `agent_messages`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID';

--
-- 使用表AUTO_INCREMENT `knowledge_chunk`
--
ALTER TABLE `knowledge_chunk`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=42;

--
-- 使用表AUTO_INCREMENT `knowledge_chunk_embedding`
--
ALTER TABLE `knowledge_chunk_embedding`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT;

--
-- 使用表AUTO_INCREMENT `knowledge_document`
--
ALTER TABLE `knowledge_document`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- 使用表AUTO_INCREMENT `operation_logs`
--
ALTER TABLE `operation_logs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID';

--
-- 使用表AUTO_INCREMENT `permissions`
--
ALTER TABLE `permissions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID', AUTO_INCREMENT=24;

--
-- 使用表AUTO_INCREMENT `privacy_data`
--
ALTER TABLE `privacy_data`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据ID', AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `risk_records`
--
ALTER TABLE `risk_records`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID', AUTO_INCREMENT=3;

--
-- 使用表AUTO_INCREMENT `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID', AUTO_INCREMENT=6;

--
-- 使用表AUTO_INCREMENT `role_permissions`
--
ALTER TABLE `role_permissions`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID', AUTO_INCREMENT=78;

--
-- 使用表AUTO_INCREMENT `security_rules`
--
ALTER TABLE `security_rules`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID', AUTO_INCREMENT=4;

--
-- 使用表AUTO_INCREMENT `sensitive_words`
--
ALTER TABLE `sensitive_words`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '敏感词ID', AUTO_INCREMENT=5;

--
-- 使用表AUTO_INCREMENT `system_configs`
--
ALTER TABLE `system_configs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID', AUTO_INCREMENT=11;

--
-- 使用表AUTO_INCREMENT `teams`
--
ALTER TABLE `teams`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '团队ID', AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `team_invitations`
--
ALTER TABLE `team_invitations`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '邀请ID';

--
-- 使用表AUTO_INCREMENT `team_join_requests`
--
ALTER TABLE `team_join_requests`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID';

--
-- 使用表AUTO_INCREMENT `team_members`
--
ALTER TABLE `team_members`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成员ID', AUTO_INCREMENT=2;

--
-- 使用表AUTO_INCREMENT `t_app`
--
ALTER TABLE `t_app`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID', AUTO_INCREMENT=21;

--
-- 使用表AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID', AUTO_INCREMENT=16;

--
-- 使用表AUTO_INCREMENT `user_login_logs`
--
ALTER TABLE `user_login_logs`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID', AUTO_INCREMENT=51;

--
-- 使用表AUTO_INCREMENT `user_roles`
--
ALTER TABLE `user_roles`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID', AUTO_INCREMENT=4;

--
-- 使用表AUTO_INCREMENT `user_verification_codes`
--
ALTER TABLE `user_verification_codes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证码ID', AUTO_INCREMENT=14;

--
-- 限制导出的表
--

--
-- 限制表 `knowledge_chunk`
--
ALTER TABLE `knowledge_chunk`
  ADD CONSTRAINT `knowledge_chunk_ibfk_1` FOREIGN KEY (`document_id`) REFERENCES `knowledge_document` (`id`) ON DELETE CASCADE;

--
-- 限制表 `agent_sessions`
--
ALTER TABLE `agent_sessions`
  ADD CONSTRAINT `fk_agent_sessions_agent` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE CASCADE;

--
-- 限制表 `agent_messages`
--
ALTER TABLE `agent_messages`
  ADD CONSTRAINT `fk_agent_messages_session` FOREIGN KEY (`session_id`) REFERENCES `agent_sessions` (`session_id`) ON DELETE CASCADE;

--
-- 限制表 `workflows`
--
ALTER TABLE `workflows`
  ADD CONSTRAINT `fk_workflows_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `workflow_nodes`
--
ALTER TABLE `workflow_nodes`
  ADD CONSTRAINT `fk_workflow_nodes_workflow_id` FOREIGN KEY (`workflow_id`) REFERENCES `workflows` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_workflow_nodes_agent_id` FOREIGN KEY (`agent_id`) REFERENCES `agents` (`id`) ON DELETE RESTRICT;

--
-- 限制表 `knowledge_chunk_embedding`
--
ALTER TABLE `knowledge_chunk_embedding`
  ADD CONSTRAINT `knowledge_chunk_embedding_ibfk_1` FOREIGN KEY (`chunk_id`) REFERENCES `knowledge_chunk` (`id`) ON DELETE CASCADE;

--
-- 限制表 `knowledge_document`
--
ALTER TABLE `knowledge_document`
  ADD CONSTRAINT `knowledge_document_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT;

--
-- 限制表 `operation_logs`
--
ALTER TABLE `operation_logs`
  ADD CONSTRAINT `fk_operation_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

--
-- 限制表 `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `fk_role_permissions_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_role_permissions_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE;

--
-- 限制表 `teams`
--
ALTER TABLE `teams`
  ADD CONSTRAINT `fk_teams_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT;

--
-- 限制表 `team_invitations`
--
ALTER TABLE `team_invitations`
  ADD CONSTRAINT `fk_team_invitations_invitee_user_id` FOREIGN KEY (`invitee_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_team_invitations_inviter_id` FOREIGN KEY (`inviter_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_team_invitations_team_id` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE;

--
-- 限制表 `team_join_requests`
--
ALTER TABLE `team_join_requests`
  ADD CONSTRAINT `fk_team_join_requests_reviewed_by` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_team_join_requests_team_id` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_team_join_requests_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `team_members`
--
ALTER TABLE `team_members`
  ADD CONSTRAINT `fk_team_members_invited_by` FOREIGN KEY (`invited_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_team_members_team_id` FOREIGN KEY (`team_id`) REFERENCES `teams` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_team_members_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `user_login_logs`
--
ALTER TABLE `user_login_logs`
  ADD CONSTRAINT `fk_login_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `fk_user_roles_assigned_by` FOREIGN KEY (`assigned_by`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- 限制表 `user_verification_codes`
--
ALTER TABLE `user_verification_codes`
  ADD CONSTRAINT `fk_verification_codes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
