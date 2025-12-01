"""
OpenAPI 规范解析器 - 将 OpenAPI 3.0 规范转换为 LangChain Tools
"""

import logging
import requests
from typing import Dict, List, Optional, Any
from langchain_core.tools import StructuredTool
from pydantic import BaseModel, Field, create_model

logger = logging.getLogger(__name__)


class APIOperation:
    """API 操作封装"""

    def __init__(
        self,
        plugin_id: str,
        operation_id: str,
        method: str,
        path: str,
        summary: str,
        description: str,
        parameters: List[Dict],
        request_body: Optional[Dict],
        base_url: str,
        auth_type: str = "none",
        auth_config: Optional[Dict] = None,
    ):
        self.plugin_id = plugin_id
        self.operation_id = operation_id
        self.method = method.upper()
        self.path = path
        self.summary = summary
        self.description = description
        self.parameters = parameters or []
        self.request_body = request_body
        self.base_url = base_url
        self.auth_type = auth_type
        self.auth_config = auth_config or {}

    def execute(self, **kwargs) -> str:
        """执行 API 调用"""
        try:
            # auth_config 现在就是运行时参数（从前端传递）
            runtime_params = self.auth_config or {}

            # 构建完整 URL
            url = self.base_url + self.path

            # 替换路径参数
            for param in self.parameters:
                if param.get("in") == "path" and param["name"] in kwargs:
                    url = url.replace(
                        f"{{{param['name']}}}", str(kwargs[param["name"]])
                    )

            # 构建查询参数
            query_params = {}
            for param in self.parameters:
                if param.get("in") == "query" and param["name"] in kwargs:
                    query_params[param["name"]] = kwargs[param["name"]]

            # 构建请求头
            headers = {"Content-Type": "application/json"}

            # 添加认证（使用运行时参数）
            if self.auth_type == "bearer":
                token = runtime_params.get("token")
                if token:
                    headers["Authorization"] = f"Bearer {token}"
            elif self.auth_type == "api_key":
                key_name = runtime_params.get("key_name", "X-API-Key")
                key_value = runtime_params.get("api_key")
                if key_value:
                    headers[key_name] = key_value

            # 添加自定义请求头（来自运行时参数）
            if "headers" in runtime_params:
                headers.update(runtime_params["headers"])

            # 添加自定义查询参数（来自运行时参数）
            if "query_params" in runtime_params:
                query_params.update(runtime_params["query_params"])

            # 构建请求体
            body = None
            if self.request_body and self.method in ["POST", "PUT", "PATCH"]:
                body = {}
                # 从 kwargs 中提取请求体参数
                if "body" in kwargs:
                    body = kwargs["body"]
                else:
                    # 尝试从 schema 中提取参数
                    schema = (
                        self.request_body.get("content", {})
                        .get("application/json", {})
                        .get("schema", {})
                    )
                    if "properties" in schema:
                        for prop_name in schema["properties"].keys():
                            if prop_name in kwargs:
                                body[prop_name] = kwargs[prop_name]

            # 发送请求
            logger.info(f"🔧 调用 API: {self.method} {url}")
            response = requests.request(
                method=self.method,
                url=url,
                params=query_params,
                json=body if body else None,
                headers=headers,
                timeout=30,
            )

            response.raise_for_status()

            # 返回响应
            if response.headers.get("content-type", "").startswith("application/json"):
                return str(response.json())
            else:
                return response.text

        except requests.exceptions.RequestException as e:
            error_msg = f"API 调用失败: {str(e)}"
            logger.error(error_msg)
            return error_msg
        except Exception as e:
            error_msg = f"执行失败: {str(e)}"
            logger.error(error_msg)
            return error_msg


def extract_required_config(
    openapi_spec: Dict, auth_type: str = "none"
) -> Dict[str, Any]:
    """
    提取插件需要的配置参数

    Args:
        openapi_spec: OpenAPI 3.0 规范
        auth_type: 认证类型

    Returns:
        配置需求描述
    """
    required_config = {
        "auth_type": auth_type,
        "required_params": [],
        "optional_params": [],
        "needs_config": auth_type != "none",  # 是否需要配置
    }

    # 如果不需要认证，直接返回
    if auth_type == "none":
        return required_config

    # 根据认证类型确定必需参数
    if auth_type == "bearer":
        required_config["required_params"].append(
            {
                "name": "token",
                "description": "Bearer Token 用于认证",
                "type": "string",
            }
        )
    elif auth_type == "api_key":
        required_config["required_params"].extend(
            [
                {
                    "name": "api_key",
                    "description": "API Key 用于认证",
                    "type": "string",
                },
                {
                    "name": "key_name",
                    "description": "API Key 的请求头名称 (默认: X-API-Key)",
                    "type": "string",
                    "default": "X-API-Key",
                },
            ]
        )

    # 检查 OpenAPI 规范中的安全要求
    security_schemes = openapi_spec.get("components", {}).get("securitySchemes", {})
    for scheme_name, scheme in security_schemes.items():
        scheme_type = scheme.get("type", "")
        if scheme_type == "http" and scheme.get("scheme") == "bearer":
            if not any(
                p["name"] == "token" for p in required_config["required_params"]
            ):
                required_config["required_params"].append(
                    {
                        "name": "token",
                        "description": f"Bearer Token ({scheme.get('description', '')})",
                        "type": "string",
                    }
                )
        elif scheme_type == "apiKey":
            key_name = scheme.get("name", "X-API-Key")
            if not any(
                p["name"] == "api_key" for p in required_config["required_params"]
            ):
                required_config["required_params"].append(
                    {
                        "name": "api_key",
                        "description": f"API Key ({scheme.get('description', '')})",
                        "type": "string",
                    }
                )
                required_config["optional_params"].append(
                    {
                        "name": "key_name",
                        "description": f"API Key 请求头名称 (默认: {key_name})",
                        "type": "string",
                        "default": key_name,
                    }
                )

    # 提取服务器 URL（可能需要配置）
    servers = openapi_spec.get("servers", [])
    if servers and "{" in servers[0].get("url", ""):
        # 服务器 URL 包含变量，需要配置
        server_vars = servers[0].get("variables", {})
        for var_name, var_info in server_vars.items():
            required_config["optional_params"].append(
                {
                    "name": f"server_{var_name}",
                    "description": var_info.get(
                        "description", f"服务器变量: {var_name}"
                    ),
                    "type": "string",
                    "default": var_info.get("default"),
                }
            )

    return required_config


def parse_openapi_plugin(
    plugin_id: str,
    openapi_spec: Dict,
    auth_type: str = "none",
    auth_config: Optional[Dict] = None,
    runtime_params: Optional[Dict] = None,
) -> List[StructuredTool]:
    """
    解析 OpenAPI 规范,生成 LangChain Tools

    Args:
        plugin_id: 插件ID
        openapi_spec: OpenAPI 3.0 规范
        auth_type: 认证类型 (none/bearer/api_key)
        auth_config: 认证配置（已弃用，使用 runtime_params）
        runtime_params: 运行时参数（从前端传递）

    Returns:
        LangChain Tool 列表
    """
    # 使用 runtime_params 替代 auth_config
    if runtime_params is None:
        runtime_params = auth_config or {}
    tools = []

    try:
        # 获取服务器地址
        servers = openapi_spec.get("servers", [])
        base_url = servers[0]["url"] if servers else ""

        # 解析路径和操作
        paths = openapi_spec.get("paths", {})

        for path, path_item in paths.items():
            for method, operation in path_item.items():
                if method.lower() not in ["get", "post", "put", "delete", "patch"]:
                    continue

                # 获取操作信息
                operation_id = operation.get("operationId", f"{method}_{path}")
                summary = operation.get("summary", "")
                description = operation.get("description", summary)
                parameters = operation.get("parameters", [])
                request_body = operation.get("requestBody")

                # 创建 API 操作（使用 runtime_params）
                api_op = APIOperation(
                    plugin_id=plugin_id,
                    operation_id=operation_id,
                    method=method,
                    path=path,
                    summary=summary,
                    description=description,
                    parameters=parameters,
                    request_body=request_body,
                    base_url=base_url,
                    auth_type=auth_type,
                    auth_config=runtime_params,  # 传递运行时参数
                )

                # 生成工具描述
                tool_description = (
                    f"{summary}\n{description}" if description else summary
                )

                # 构建参数 schema (Pydantic format)
                from pydantic import create_model, Field as PydanticField

                fields = {}
                param_descriptions = []

                for param in parameters:
                    param_name = param.get("name", "")
                    param_desc_text = param.get("description", "")
                    param_required = param.get("required", False)
                    param_schema = param.get("schema", {})
                    param_type = param_schema.get("type", "string")

                    # 映射 OpenAPI 类型到 Python 类型
                    type_mapping = {
                        "string": str,
                        "integer": int,
                        "number": float,
                        "boolean": bool,
                    }
                    py_type = type_mapping.get(param_type, str)

                    # 构建字段
                    if param_required:
                        fields[param_name] = (
                            py_type,
                            PydanticField(..., description=param_desc_text),
                        )
                    else:
                        default = param_schema.get("default", None)
                        fields[param_name] = (
                            py_type,
                            PydanticField(default=default, description=param_desc_text),
                        )

                    # 添加到描述
                    required_text = "必需" if param_required else "可选"
                    param_descriptions.append(
                        f"  - {param_name} ({required_text}): {param_desc_text}"
                    )

                # 添加参数说明到描述
                if param_descriptions:
                    tool_description += "\n参数:\n" + "\n".join(param_descriptions)

                # 创建参数模型
                if fields:
                    ArgsSchema = create_model(f"{operation_id}_args", **fields)
                else:
                    ArgsSchema = None

                # 创建 LangChain Tool
                tool = StructuredTool.from_function(
                    name=operation_id,
                    description=tool_description[:1000],  # 限制长度
                    func=api_op.execute,
                    args_schema=ArgsSchema,  # 传递参数 schema
                )

                tools.append(tool)
                logger.info(f"  ✅ 创建工具: {operation_id}")

        logger.info(f"✅ 插件 {plugin_id} 解析完成,生成 {len(tools)} 个工具")

    except Exception as e:
        logger.error(f"解析 OpenAPI 规范失败: {str(e)}")

    return tools
