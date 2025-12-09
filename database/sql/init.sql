-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： mysql:3306
-- 生成日期： 2025-12-09 06:22:59
-- 服务器版本： 8.0.43
-- PHP 版本： 8.3.26

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `new_db`
--
CREATE DATABASE IF NOT EXISTS `new_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `new_db`;

-- --------------------------------------------------------

--
-- 表的结构 `knowledge_bases`
--

CREATE TABLE `knowledge_bases` (
  `kb_id` varchar(128) NOT NULL COMMENT '知识库唯一标识(user_id+name生成)',
  `user_id` varchar(64) NOT NULL COMMENT '所属用户ID',
  `name` varchar(128) NOT NULL COMMENT '知识库名称',
  `description` text COMMENT '知识库描述',
  `file_paths` json NOT NULL COMMENT '文档文件路径列表(JSON数组)',
  `chunking_method` varchar(64) DEFAULT NULL COMMENT 'chunking方式: recursive, fixed, semantic',
  `chunk_size` int DEFAULT NULL COMMENT 'chunk大小',
  `chunk_overlap` int DEFAULT NULL COMMENT 'chunk重叠',
  `total_chunks` int DEFAULT NULL COMMENT '总chunk数量',
  `milvus_collection` varchar(128) DEFAULT NULL COMMENT 'Milvus集合名称',
  `enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `created_at` varchar(64) DEFAULT NULL COMMENT '创建时间',
  `updated_at` varchar(64) DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- 转存表中的数据 `knowledge_bases`
--

INSERT INTO `knowledge_bases` (`kb_id`, `user_id`, `name`, `description`, `file_paths`, `chunking_method`, `chunk_size`, `chunk_overlap`, `total_chunks`, `milvus_collection`, `enabled`, `created_at`, `updated_at`) VALUES
('test_user_0937125794172847', 'test_user', '机器学习知识库-1765108947', '包含机器学习基础知识的知识库', '[\"knowledge\\\\origin_document\\\\test_user_0937125794172847\\\\machine_learning.txt\"]', 'recursive', 800, 150, 1, 'test_user_0937125794172847', 1, '2025-12-07T20:02:42.277427', '2025-12-07T20:02:42.277427'),
('test_user_1833dc23e32449cb', 'test_user', '机器学习知识库-1765108530', '包含机器学习基础知识的知识库', '[\"knowledge\\\\origin_document\\\\test_user_1833dc23e32449cb\\\\machine_learning.txt\"]', 'recursive', 800, 150, 1, 'test_user_1833dc23e32449cb', 1, '2025-12-07T19:55:35.493176', '2025-12-07T19:55:35.493176'),
('test_user_8167dafc043c6e14', 'test_user', '机器学习知识库', '包含机器学习基础知识的知识库', '[\"knowledge\\\\origin_document\\\\test_user_8167dafc043c6e14\\\\machine_learning.txt\"]', 'recursive', 800, 150, 1, 'test_user_8167dafc043c6e14', 1, '2025-12-07T19:43:35.058218', '2025-12-07T19:43:35.058218'),
('test_user_be573f5d7e5f8cd6', 'test_user', 'test_kb_create_query', '用于测试的知识库', '[\"knowledge\\\\origin_document\\\\test_user_be573f5d7e5f8cd6\\\\ai_history.txt\", \"knowledge\\\\origin_document\\\\test_user_be573f5d7e5f8cd6\\\\deep_learning.txt\", \"knowledge\\\\origin_document\\\\test_user_be573f5d7e5f8cd6\\\\machine_learning.txt\"]', 'recursive', 500, 50, 7, 'test_user_be573f5d7e5f8cd6', 1, '2025-12-06T10:42:35.713812', '2025-12-06T10:42:35.713812'),
('test_user_d1ca6fb531918b7d', 'test_user', '机器学习知识库-1765109082', '包含机器学习基础知识的知识库', '[\"knowledge\\\\origin_document\\\\test_user_d1ca6fb531918b7d\\\\machine_learning.txt\"]', 'recursive', 800, 150, 1, 'test_user_d1ca6fb531918b7d', 1, '2025-12-07T20:04:56.753360', '2025-12-07T20:04:56.753360');

-- --------------------------------------------------------

--
-- 表的结构 `models`
--

CREATE TABLE `models` (
  `model_id` varchar(64) NOT NULL COMMENT '模型唯一标识',
  `display_name` varchar(128) NOT NULL COMMENT '显示名称',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API密钥',
  `base_url` varchar(512) DEFAULT NULL COMMENT 'API基础URL',
  `model` varchar(128) NOT NULL COMMENT '实际模型名称',
  `max_tokens` int DEFAULT NULL COMMENT '最大输出长度',
  `temperature` float DEFAULT NULL COMMENT '创意/随机性',
  `top_p` float DEFAULT NULL COMMENT 'nucleus sampling',
  `top_k` int DEFAULT NULL COMMENT 'RAG/核心采样候选词数量',
  `frequency_penalty` float DEFAULT NULL COMMENT '避免重复',
  `presence_penalty` float DEFAULT NULL COMMENT '鼓励新话题',
  `stop_sequences` json DEFAULT NULL COMMENT '停止序列',
  `stream` tinyint(1) DEFAULT NULL COMMENT '流式输出',
  `timeout` int DEFAULT NULL COMMENT '超时时间(秒)',
  `retry_max_attempts` int DEFAULT NULL COMMENT '最大重试次数',
  `retry_backoff_factor` int DEFAULT NULL COMMENT '重试退避因子',
  `enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `description` text COMMENT '模型描述'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- 转存表中的数据 `models`
--

INSERT INTO `models` (`model_id`, `display_name`, `api_key`, `base_url`, `model`, `max_tokens`, `temperature`, `top_p`, `top_k`, `frequency_penalty`, `presence_penalty`, `stop_sequences`, `stream`, `timeout`, `retry_max_attempts`, `retry_backoff_factor`, `enabled`, `description`) VALUES
('qwen-max', '通义千问-Max', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen-max', 8000, 0.7, 0.9, 50, 0, 0, '[\"\\n\\n\"]', 1, 30, 3, 2, 1, '自动导入的 qwen-max 模型配置'),
('qwen-mt-plus', '通义千问mt-plus', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen-mt-plus', 1024, 0.7, 0.9, 50, 0, 0, '[\"\\n\\n\"]', 1, 30, 3, 2, 1, '自动导入的 qwen-mt-plus 模型配置'),
('qwen3-max', '通义千问3-Max', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen3-max', 32000, 0.7, 0.9, 50, 0, 0, '[\"\\n\\n\"]', 1, 30, 3, 2, 1, '自动导入的 qwen3-max 模型配置');

-- --------------------------------------------------------

--
-- 表的结构 `plugin_tools`
--

CREATE TABLE `plugin_tools` (
  `tool_id` varchar(64) NOT NULL COMMENT '工具唯一标识',
  `name` varchar(128) NOT NULL COMMENT '工具名称',
  `purpose` text COMMENT '工具用途',
  `version` varchar(32) DEFAULT NULL COMMENT '工具版本',
  `call_method` varchar(256) NOT NULL COMMENT '调用方法，如 GET https://api.example.com/endpoint',
  `parameters` json DEFAULT NULL COMMENT '参数定义(JSON数组)，包含参数名、类型、必填、枚举值等',
  `user_settings` json DEFAULT NULL COMMENT '用户设定的参数值，如 {uuid: xxx, api_key: yyy}',
  `created_at` varchar(64) DEFAULT NULL COMMENT '创建时间',
  `updated_at` varchar(64) DEFAULT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- 转存表中的数据 `plugin_tools`
--

INSERT INTO `plugin_tools` (`tool_id`, `name`, `purpose`, `version`, `call_method`, `parameters`, `user_settings`, `created_at`, `updated_at`) VALUES
('test_user_getWeatherInfo', 'getWeatherInfo', '查询天气信息 - 根据城市名字查询当前或未来天气', '1.0.0', 'GET https://restapi.amap.com/v3/weather/weatherInfo', '\"  - key (query): 高德 Web API key，必填: True\\n  - city (query): 城市名字例如：北京，必填: True\\n  - extensions (query): base=实况天气；all=预报天气，必填: False, 枚举值: [\'base\', \'all\']\\n  - output (query): 返回格式，默认 JSON，必填: False, 枚举值: [\'JSON\', \'XML\']\"', '{}', '2025-12-07T20:04:42.829602', '2025-12-07T20:04:42.829602');

--
-- 转储表的索引
--

--
-- 表的索引 `knowledge_bases`
--
ALTER TABLE `knowledge_bases`
  ADD PRIMARY KEY (`kb_id`);

--
-- 表的索引 `models`
--
ALTER TABLE `models`
  ADD PRIMARY KEY (`model_id`);

--
-- 表的索引 `plugin_tools`
--
ALTER TABLE `plugin_tools`
  ADD PRIMARY KEY (`tool_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
