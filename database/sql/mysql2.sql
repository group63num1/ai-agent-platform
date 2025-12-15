-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： mysql2:3306
-- 生成日期： 2025-12-15 05:37:14
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

INSERT INTO `models` (`model_id`, `display_name`, `api_key`, `base_url`, `model`, `max_tokens`, `temperature`, `top_p`, `top_k`, `frequency_penalty`, `presence_penalty`, `stream`, `timeout`, `retry_max_attempts`, `retry_backoff_factor`, `enabled`, `description`) VALUES
('deepseek-r1', 'deepseek-r1', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'deepseek-r1', 16000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'DeepSeek-R1 在后训练阶段大规模使用了强化学习技术，在仅有极少标注数据的情况下，极大提升了模型推理能力。在数学、代码、自然语言推理等任务上，性能较高，能力较强。'),
('deepseek-v3.2', 'deepseek-v3.2', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'deepseek-v3.2', 64000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'DeepSeek-V3.2是引入DeepSeek Sparse Attention（一种稀疏注意力机制）的正式版模型，也是DeepSeek推出的首个将思考融入工具使用的模型，同时支持思考模式与非思考模式的工具调用。'),
('glm-4.6', '智谱 GLM-4.6 ', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'glm-4.6', 16000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'GLM新一代旗舰模型，核心能力较4.5全面提升。总参数量为3550 亿，激活参数320亿，上下文窗口扩展至200K。'),
('kimi-k2-thinking', 'kimi-k2', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'kimi-k2-thinking', 16000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'kimi-k2-thinking模型是月之暗面提供的具有通用 Agentic能力和推理能力的思考模型，它擅长深度推理，并可通过多步工具调用，帮助解决各类难题。'),
('qwen-flash', '通义千问-Flash', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen-flash', 32000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'Qwen3系列Flash模型，实现思考模式和非思考模式的有效融合，可在对话中切换模式。复杂推理类任务性能优秀，指令遵循、文本理解等能力显著提高。'),
('qwen-max', '通义千问-Max', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen-max', 8000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, '通义千问2.5系列千亿级别超大规模语言模型，支持中文、英文等不同语言输入。'),
('qwen-plus', '通义千问-Plus', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen-plus', 32000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, 'Qwen3系列Plus模型，实现思考模式和非思考模式的有效融合，可在对话中切换模式。推理能力显著超过QwQ、通用能力显著超过Qwen2.5-Plus，达到同规模业界SOTA水平。'),
('qwen3-max', '通义千问3-Max', 'sk-24c630328e3d478aa7a8156ac1ab6dca', 'https://dashscope.aliyuncs.com/compatible-mode/v1', 'qwen3-max', 32000, 0.5, 0.9, 50, 0, 0, 1, 30, 3, 2, 1, '通义千问3系列Max模型，相较preview版本在智能体编程与工具调用方向进行了专项升级。本次发布的正式版模型达到领域SOTA水平，适配场景更加复杂的智能体需求。');

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
('test_user_controlDevice', 'controlDevice', '控制设备 - 控制LED、继电器、舵机、PWM等设备', '1.2.0', 'POST https://plugin.aiot.hello1023.com/plugin/control', '\"  - device_uuid (body): 设备UUID，必填: True\\n  - port_type (body): 设备类型：led(LED灯)、relay(继电器)、servo(舵机)、pwm(PWM输出)，必填: True, 枚举值: [\'led\', \'relay\', \'servo\', \'pwm\']\\n  - port_id (body): 端口ID：LED和继电器为1-4，舵机为1-4，PWM为1-2，必填: True\\n  - action (body): 动作：on(打开)/off(关闭)/set(设置值，用于舵机角度或PWM占空比)，必填: True, 枚举值: [\'on\', \'off\', \'set\']\\n  - value (body): 设置值：舵机角度(0-180)或PWM占空比(0-100)，仅当action为set时需要，必填: False\"', '\"uuid=c10366db-d8c2-4914-92cb-b5a888520488\"', '2025-12-14T21:31:04.292061', '2025-12-14T21:31:04.292061'),
('test_user_executePreset', 'executePreset', '执行预设 - 通过preset_key执行用户自定义预设', '1.2.0', 'POST https://plugin.aiot.hello1023.com/plugin/preset', '\"  - device_uuid (body): 设备UUID，必填: True\\n  - preset_name (body): 预设标识(preset_key)，如：led_blink_k8x9y2，必填: True\\n  - parameters (body): 可选参数（通常为空），必填: False\"', '\"uuid=c10366db-d8c2-4914-92cb-b5a888520488\"', '2025-12-14T21:31:04.300135', '2025-12-14T21:31:04.300135'),
('test_user_getSensorData', 'getSensorData', '查询传感器 - 获取各类传感器数据（温度、湿度、雨水、DS18B20等）', '1.2.0', 'GET https://plugin.aiot.hello1023.com/plugin/sensor-data', '\"  - uuid (query): UUID，必填: True\\n  - sensor (query): 传感器类型，必填: True, 枚举值: [\'温度\', \'湿度\', \'雨水\', \'雨水级别\', \'DS18B20\', \'DS18B20温度\', \'temperature\', \'humidity\', \'rain\', \'rain_level\']\"', '\"uuid=c10366db-d8c2-4914-92cb-b5a888520488\"', '2025-12-14T21:31:04.284063', '2025-12-14T21:31:04.284063'),
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
