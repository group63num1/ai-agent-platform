package com.example.demo.service.impl;

import com.example.demo.app.entity.Plugin;
import com.example.demo.app.entity.PluginTool;
import com.example.demo.dto.PluginTestResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class PythonAgentClient {

    private static final Logger logger = LoggerFactory.getLogger(PythonAgentClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${python.agent.base-url:http://localhost:9000}")
    private String pythonAgentBaseUrl;

    public PythonAgentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PluginTestResultDTO testPluginTool(Plugin plugin, PluginTool tool, Map<String, Object> inputs) {
        PluginTestResultDTO result = new PluginTestResultDTO();
        try {
            Map<String, Object> payload = new HashMap<>();
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("name", plugin.getName());
            pluginInfo.put("pluginUrl", plugin.getPluginUrl());
            pluginInfo.put("authType", plugin.getAuthType());
            pluginInfo.put("headers", plugin.getHeadersJson());
            pluginInfo.put("specJson", plugin.getSpecJson());

            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", tool.getName());
            toolInfo.put("method", tool.getMethod());
            toolInfo.put("endpoint", tool.getEndpoint());
            toolInfo.put("inputParams", tool.getInputParamsJson());
            toolInfo.put("outputParams", tool.getOutputParamsJson());
            toolInfo.put("requestExample", tool.getRequestExample());

            payload.put("plugin", pluginInfo);
            payload.put("tool", toolInfo);
            payload.put("inputs", inputs);

            String url = pythonAgentBaseUrl + "/api/agent/plugins/test";
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            result.setSuccess(response.getStatusCode().is2xxSuccessful());
            result.setResponse(response.getBody());
            result.setMessage(result.isSuccess() ? "测试通过" : "测试失败");
            return result;
        } catch (RestClientException e) {
            logger.error("调用 Python Agent 失败", e);
            result.setSuccess(false);
            result.setMessage("Python Agent 不可用: " + e.getMessage());
            return result;
        }
    }
}


