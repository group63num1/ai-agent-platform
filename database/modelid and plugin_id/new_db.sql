-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： mysql2:3306
-- 生成日期： 2025-12-01 19:44:31
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

-- --------------------------------------------------------

--
-- 表的结构 `models`
--

CREATE TABLE `models` (
  `model_id` varchar(64) NOT NULL COMMENT '模型唯一标识',
  `display_name` varchar(128) NOT NULL COMMENT '显示名称',
  `model` varchar(128) NOT NULL COMMENT '实际模型名称',
  `api_key` varchar(512) DEFAULT NULL COMMENT 'API密钥',
  `base_url` varchar(512) DEFAULT NULL COMMENT 'API基础URL',
  `enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `description` text COMMENT '模型描述',
  `max_tokens` int DEFAULT NULL COMMENT '最大token数',
  `temperature` varchar(16) DEFAULT NULL COMMENT '温度参数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- 转存表中的数据 `models`
--

INSERT INTO `models` (`model_id`, `display_name`, `model`, `api_key`, `base_url`, `enabled`, `description`, `max_tokens`, `temperature`) VALUES
('gpt-3.5-turbo', 'GPT-3.5 Turbo', 'gpt-3.5-turbo', '', 'https://api.openai.com/v1', 0, 'OpenAI GPT-3.5 Turbo模型', NULL, NULL),
('qwen-plus', '通义千问Plus', 'qwen-plus', '', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0, '阿里云通义千问Plus模型', NULL, NULL);

-- --------------------------------------------------------

--
-- 表的结构 `plugins`
--

CREATE TABLE `plugins` (
  `plugin_id` varchar(64) NOT NULL COMMENT '插件唯一标识',
  `plugin_name` varchar(128) NOT NULL COMMENT '插件名称',
  `description` text COMMENT '插件描述',
  `openapi_spec` json NOT NULL COMMENT 'OpenAPI 3.0 规范 JSON',
  `enabled` tinyint(1) DEFAULT NULL COMMENT '是否启用',
  `auth_type` varchar(32) DEFAULT NULL COMMENT '认证类型: none/bearer/apikey',
  `auth_config` json DEFAULT NULL COMMENT '认证配置'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- 转存表中的数据 `plugins`
--

INSERT INTO `plugins` (`plugin_id`, `plugin_name`, `description`, `openapi_spec`, `enabled`, `auth_type`, `auth_config`) VALUES
('001b5d60-e771-4f21-b303-0db8f8f8a5f8', 'weather_query', '查询指定城市的天气信息', '{\"info\": {\"title\": \"天气查询API\", \"version\": \"1.0.0\", \"description\": \"提供城市天气查询功能\"}, \"paths\": {\"/{city}\": {\"get\": {\"summary\": \"查询指定城市的天气\", \"responses\": {\"200\": {\"description\": \"成功返回天气数据\"}}, \"parameters\": [{\"in\": \"path\", \"name\": \"city\", \"schema\": {\"type\": \"string\"}, \"required\": true}, {\"in\": \"query\", \"name\": \"format\", \"schema\": {\"type\": \"string\", \"default\": \"j1\"}}, {\"in\": \"query\", \"name\": \"lang\", \"schema\": {\"type\": \"string\", \"default\": \"zh\"}}], \"operationId\": \"weather_query\"}}}, \"openapi\": \"3.0.0\", \"servers\": [{\"url\": \"http://wttr.in\", \"description\": \"天气API服务器\"}]}', 1, 'none', 'null'),
('17d4b51d-2867-418c-805e-20007ed30071', 'weather_query', '查询指定城市的天气信息', '{\"info\": {\"title\": \"天气查询API\", \"version\": \"1.0.0\", \"description\": \"提供城市天气查询功能\"}, \"paths\": {\"/{city}\": {\"get\": {\"summary\": \"查询指定城市的天气\", \"responses\": {\"200\": {\"description\": \"成功返回天气数据\"}}, \"parameters\": [{\"in\": \"path\", \"name\": \"city\", \"schema\": {\"type\": \"string\"}, \"required\": true}, {\"in\": \"query\", \"name\": \"format\", \"schema\": {\"type\": \"string\", \"default\": \"j1\"}}, {\"in\": \"query\", \"name\": \"lang\", \"schema\": {\"type\": \"string\", \"default\": \"zh\"}}], \"operationId\": \"weather_query\"}}}, \"openapi\": \"3.0.0\", \"servers\": [{\"url\": \"http://wttr.in\", \"description\": \"天气API服务器\"}]}', 1, 'none', 'null');

--
-- 转储表的索引
--

--
-- 表的索引 `models`
--
ALTER TABLE `models`
  ADD PRIMARY KEY (`model_id`);

--
-- 表的索引 `plugins`
--
ALTER TABLE `plugins`
  ADD PRIMARY KEY (`plugin_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
