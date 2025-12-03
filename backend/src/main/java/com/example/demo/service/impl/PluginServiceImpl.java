package com.example.demo.service.impl;

import com.example.demo.app.entity.Plugin;
import com.example.demo.app.entity.PluginTool;
import com.example.demo.app.mapper.PluginMapper;
import com.example.demo.app.mapper.PluginToolMapper;
import com.example.demo.dto.*;
import com.example.demo.service.PluginService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PluginServiceImpl implements PluginService {

    private static final String STATUS_DRAFT = "draft";
    private static final String STATUS_ENABLED = "enabled";
    private static final String STATUS_DISABLED = "disabled";
    private static final String PUBLISH_UNPUBLISHED = "unpublished";
    private static final String PUBLISH_PUBLISHED = "published";

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private PluginToolMapper pluginToolMapper;

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<PluginSummaryDTO> listPlugins() {
        List<Plugin> plugins = pluginMapper.selectAll();
        return plugins.stream().map(this::toSummaryDTO).collect(Collectors.toList());
    }

    @Override
    public PluginDTO getPluginDetail(Long pluginId) {
        Plugin plugin = requirePlugin(pluginId);
        List<PluginTool> tools = pluginToolMapper.selectByPluginId(pluginId);
        return toDetailDTO(plugin, tools);
    }

    @Override
    @Transactional
    public PluginDTO createPlugin(Long userId, PluginCreateRequest request) {
        Plugin plugin = new Plugin();
        plugin.setUserId(userId);
        applyRequest(plugin, request);
        plugin.setStatus(STATUS_DRAFT);
        plugin.setPublishStatus(PUBLISH_UNPUBLISHED);
        plugin.setTestStatus("pending");
        plugin.setEnabled(false);
        LocalDateTime now = LocalDateTime.now();
        plugin.setCreatedAt(now);
        plugin.setUpdatedAt(now);
        pluginMapper.insert(plugin);
        saveTools(plugin.getId(), request.getTools());
        return getPluginDetail(plugin.getId());
    }

    @Override
    @Transactional
    public PluginDTO updatePlugin(Long pluginId, PluginUpdateRequest request) {
        Plugin plugin = requirePlugin(pluginId);
        applyRequest(plugin, request);
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginMapper.update(plugin);

        pluginToolMapper.deleteByPluginId(pluginId);
        saveTools(pluginId, request.getTools());

        return getPluginDetail(pluginId);
    }

    @Override
    @Transactional
    public void deletePlugin(Long pluginId) {
        requirePlugin(pluginId);
        pluginToolMapper.deleteByPluginId(pluginId);
        pluginMapper.delete(pluginId);
    }

    @Override
    @Transactional
    public PluginDTO togglePlugin(Long pluginId, boolean enable) {
        Plugin plugin = requirePlugin(pluginId);
        if (enable && !"passed".equalsIgnoreCase(plugin.getTestStatus())) {
            throw new RuntimeException("请先通过试运行后再启用插件");
        }
        plugin.setEnabled(enable);
        plugin.setStatus(enable ? STATUS_ENABLED : STATUS_DISABLED);
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginMapper.update(plugin);
        return getPluginDetail(pluginId);
    }

    @Override
    @Transactional
    public PluginDTO publishPlugin(Long pluginId) {
        Plugin plugin = requirePlugin(pluginId);
        if (plugin.getEnabled() == null || !plugin.getEnabled()) {
            throw new RuntimeException("启用插件后才能发布");
        }
        plugin.setPublishStatus(PUBLISH_PUBLISHED);
        plugin.setUpdatedAt(LocalDateTime.now());
        pluginMapper.update(plugin);
        return getPluginDetail(pluginId);
    }

    @Override
    @Transactional
    public PluginTestResultDTO testPluginTool(PluginTestRequest request) {
        Plugin plugin = requirePlugin(request.getPluginId());
        PluginTool tool = pluginToolMapper.selectById(request.getToolId());
        if (tool == null || !Objects.equals(tool.getPluginId(), plugin.getId())) {
            throw new RuntimeException("工具不存在");
        }
        Map<String, Object> inputs = request.getInputs() != null ? request.getInputs() : Collections.emptyMap();
        PluginTestResultDTO result = executePluginRequest(plugin, tool, inputs);
        if (result.isSuccess()) {
            tool.setTestStatus("passed");
            plugin.setTestStatus("passed");
        } else {
            tool.setTestStatus("failed");
            plugin.setTestStatus("failed");
        }
        tool.setLastTestAt(LocalDateTime.now());
        plugin.setLastTestAt(LocalDateTime.now());
        pluginToolMapper.update(tool);
        pluginMapper.update(plugin);
        return result;
    }

    @Override
    public PluginTemplateDTO importTemplate(MultipartFile file) {
        try {
            String raw = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(raw);
            PluginCreateRequest request = new PluginCreateRequest();
            if (root.has("info")) {
                JsonNode info = root.get("info");
                request.setName(info.path("title").asText(""));
                request.setDescription(info.path("description").asText(""));
                request.setIntro(info.path("description").asText(""));
                request.setVersion(info.path("version").asText("1.0.0"));
            }
            if (root.has("servers") && root.get("servers").isArray() && root.get("servers").size() > 0) {
                request.setPluginUrl(root.get("servers").get(0).path("url").asText(""));
            }

            List<PluginToolDTO> tools = new ArrayList<>();
            JsonNode paths = root.path("paths");
            if (paths.isObject()) {
                Iterator<String> it = paths.fieldNames();
                while (it.hasNext()) {
                    String path = it.next();
                    JsonNode pathNode = paths.get(path);
                    Iterator<String> methodIt = pathNode.fieldNames();
                    while (methodIt.hasNext()) {
                        String method = methodIt.next();
                        JsonNode methodNode = pathNode.get(method);
                        PluginToolDTO toolDTO = new PluginToolDTO();
                        toolDTO.setName(methodNode.path("operationId").asText(UUID.randomUUID().toString()));
                        toolDTO.setDescription(methodNode.path("description").asText(""));
                        toolDTO.setMethod(method.toUpperCase(Locale.ROOT));
                        toolDTO.setEndpoint(path);
                        toolDTO.setInputParams(parseParameters(methodNode));
                        toolDTO.setOutputParams(parseResponseParameters(methodNode));
                        tools.add(toolDTO);
                    }
                }
            }
            request.setTools(tools);
            request.setSpecJson(raw);
            PluginTemplateDTO templateDTO = new PluginTemplateDTO();
            templateDTO.setTemplate(request);
            return templateDTO;
        } catch (IOException e) {
            throw new RuntimeException("解析插件模板失败: " + e.getMessage(), e);
        }
    }

    private PluginTestResultDTO executePluginRequest(Plugin plugin, PluginTool tool, Map<String, Object> inputs) {
        PluginTestResultDTO result = new PluginTestResultDTO();
        JsonNode specRoot = parseSpec(plugin);
        try {
            String baseUrl = resolveBaseUrl(plugin, specRoot);
            String endpoint = resolveEndpoint(plugin, tool, specRoot);
            String method = resolveMethod(tool, specRoot, endpoint);
            if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(endpoint) || StringUtils.isBlank(method)) {
                throw new RuntimeException("插件URL或请求方式配置不完整");
            }
            String fullUrl = buildUrl(baseUrl, endpoint);

            HttpHeaders headers = new HttpHeaders();
            for (PluginHeaderDTO header : readHeaders(plugin.getHeadersJson())) {
                if (StringUtils.isNotBlank(header.getKey())) {
                    headers.add(header.getKey(), header.getValue());
                }
            }

            ResponseEntity<String> response;
            if ("GET".equalsIgnoreCase(method)) {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(fullUrl);
                inputs.forEach((key, value) -> builder.queryParam(key, value));
                response = restTemplate.exchange(builder.build(true).toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
            } else {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(inputs, headers);
                response = restTemplate.exchange(fullUrl, HttpMethod.valueOf(method.toUpperCase(Locale.ROOT)), entity, String.class);
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                result.setSuccess(true);
                result.setMessage("请求成功");
                result.setResponse(response.getBody());
            } else {
                result.setSuccess(false);
                result.setMessage("目标服务返回状态码：" + response.getStatusCode());
                result.setResponse(response.getBody());
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("目标服务调用失败: " + e.getMessage());
            result.setResponse(null);
        }
        return result;
    }

    private JsonNode parseSpec(Plugin plugin) {
        if (StringUtils.isBlank(plugin.getSpecJson())) {
            return null;
        }
        try {
            return objectMapper.readTree(plugin.getSpecJson());
        } catch (Exception e) {
            return null;
        }
    }

    private String resolveBaseUrl(Plugin plugin, JsonNode specRoot) {
        if (StringUtils.isNotBlank(plugin.getPluginUrl())) {
            return plugin.getPluginUrl();
        }
        if (specRoot != null) {
            JsonNode servers = specRoot.path("servers");
            if (servers.isArray() && servers.size() > 0) {
                String url = servers.get(0).path("url").asText("");
                if (StringUtils.isNotBlank(url)) {
                    return url;
                }
            }
        }
        return null;
    }

    private String resolveEndpoint(Plugin plugin, PluginTool tool, JsonNode specRoot) {
        if (StringUtils.isNotBlank(tool.getEndpoint())) {
            return tool.getEndpoint();
        }
        JsonNode pathNode = findPathNode(specRoot, null);
        if (pathNode != null) {
            return pathNode.path("_endpoint").asText("");
        }
        return null;
    }

    private String resolveMethod(PluginTool tool, JsonNode specRoot, String endpoint) {
        if (StringUtils.isNotBlank(tool.getMethod())) {
            return tool.getMethod();
        }
        JsonNode pathNode = findPathNode(specRoot, endpoint);
        if (pathNode != null && pathNode.has("_method")) {
            return pathNode.get("_method").asText("POST");
        }
        return "POST";
    }

    private JsonNode findPathNode(JsonNode specRoot, String endpoint) {
        if (specRoot == null) {
            return null;
        }
        JsonNode paths = specRoot.path("paths");
        if (!paths.isObject()) {
            return null;
        }
        if (StringUtils.isNotBlank(endpoint) && paths.has(endpoint)) {
            JsonNode node = paths.get(endpoint);
            enrichPathMetadata(node, endpoint);
            return node;
        }
        Iterator<String> it = paths.fieldNames();
        if (it.hasNext()) {
            String first = it.next();
            JsonNode node = paths.get(first);
            enrichPathMetadata(node, first);
            return node;
        }
        return null;
    }

    private void enrichPathMetadata(JsonNode pathNode, String endpoint) {
        if (pathNode == null || !(pathNode.isObject())) {
            return;
        }
        Iterator<String> methods = pathNode.fieldNames();
        if (methods.hasNext()) {
            String method = methods.next();
            ((com.fasterxml.jackson.databind.node.ObjectNode) pathNode).put("_method", method.toUpperCase(Locale.ROOT));
            ((com.fasterxml.jackson.databind.node.ObjectNode) pathNode).put("_endpoint", endpoint);
        }
    }

    private String buildUrl(String baseUrl, String endpoint) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedEndpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return normalizedBase + normalizedEndpoint;
    }

    private List<PluginParamDTO> parseParameters(JsonNode methodNode) {
        List<PluginParamDTO> params = new ArrayList<>();
        JsonNode parameters = methodNode.path("parameters");
        if (parameters.isArray()) {
            for (JsonNode paramNode : parameters) {
                PluginParamDTO param = new PluginParamDTO();
                param.setName(paramNode.path("name").asText());
                param.setDescription(paramNode.path("description").asText(""));
                param.setType(paramNode.path("schema").path("type").asText("string"));
                param.setLocation(paramNode.path("in").asText("query"));
                param.setRequired(paramNode.path("required").asBoolean(false));
                param.setExample(paramNode.path("schema").path("example").asText(""));
                params.add(param);
            }
        }
        JsonNode requestBody = methodNode.path("requestBody").path("content").path("application/json").path("schema");
        if (requestBody.isObject()) {
            JsonNode properties = requestBody.path("properties");
            Iterator<String> fieldNames = properties.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode propertyNode = properties.get(field);
                PluginParamDTO param = new PluginParamDTO();
                param.setName(field);
                param.setDescription(propertyNode.path("description").asText(""));
                param.setType(propertyNode.path("type").asText("string"));
                param.setLocation("body");
                param.setRequired(requestBody.path("required").toString().contains(field));
                param.setExample(propertyNode.path("example").asText(""));
                params.add(param);
            }
        }
        return params;
    }

    private List<PluginParamDTO> parseResponseParameters(JsonNode methodNode) {
        List<PluginParamDTO> params = new ArrayList<>();
        JsonNode responses = methodNode.path("responses").path("200").path("content").path("application/json").path("schema");
        if (responses.isObject()) {
            JsonNode properties = responses.path("properties");
            Iterator<String> fieldNames = properties.fieldNames();
            while (fieldNames.hasNext()) {
                String field = fieldNames.next();
                JsonNode propertyNode = properties.get(field);
                PluginParamDTO param = new PluginParamDTO();
                param.setName(field);
                param.setDescription(propertyNode.path("description").asText(""));
                param.setType(propertyNode.path("type").asText("string"));
                param.setLocation("response");
                param.setRequired(false);
                param.setExample(propertyNode.path("example").asText(""));
                params.add(param);
            }
        }
        return params;
    }

    private PluginSummaryDTO toSummaryDTO(Plugin plugin) {
        PluginSummaryDTO dto = new PluginSummaryDTO();
        dto.setId(plugin.getId());
        dto.setName(plugin.getName());
        dto.setDescription(plugin.getDescription());
        dto.setVersion(plugin.getVersion());
        dto.setStatus(plugin.getStatus());
        dto.setTestStatus(plugin.getTestStatus());
        dto.setPublishStatus(plugin.getPublishStatus());
        dto.setEnabled(plugin.getEnabled());
        dto.setIconUrl(plugin.getIconUrl());
        dto.setCreatedAt(plugin.getCreatedAt());
        dto.setUpdatedAt(plugin.getUpdatedAt());
        return dto;
    }

    private PluginDTO toDetailDTO(Plugin plugin, List<PluginTool> tools) {
        PluginDTO dto = new PluginDTO();
        dto.setId(plugin.getId());
        dto.setName(plugin.getName());
        dto.setIntro(plugin.getIntro());
        dto.setDescription(plugin.getDescription());
        dto.setIconUrl(plugin.getIconUrl());
        dto.setPluginType(plugin.getPluginType());
        dto.setCreateMode(plugin.getCreateMode());
        dto.setVersion(plugin.getVersion());
        dto.setStatus(plugin.getStatus());
        dto.setPublishStatus(plugin.getPublishStatus());
        dto.setTestStatus(plugin.getTestStatus());
        dto.setEnabled(plugin.getEnabled());
        dto.setPluginUrl(plugin.getPluginUrl());
        dto.setAuthType(plugin.getAuthType());
        dto.setHeaders(readHeaders(plugin.getHeadersJson()));
        dto.setSpecJson(plugin.getSpecJson());
        dto.setLastTestAt(plugin.getLastTestAt());
        dto.setCreatedAt(plugin.getCreatedAt());
        dto.setUpdatedAt(plugin.getUpdatedAt());
        dto.setTools(tools.stream().map(this::toToolDTO).collect(Collectors.toList()));
        return dto;
    }

    private PluginToolDTO toToolDTO(PluginTool tool) {
        PluginToolDTO dto = new PluginToolDTO();
        dto.setId(tool.getId());
        dto.setPluginId(tool.getPluginId());
        dto.setName(tool.getName());
        dto.setDescription(tool.getDescription());
        dto.setMethod(tool.getMethod());
        dto.setEndpoint(tool.getEndpoint());
        dto.setServiceStatus(tool.getServiceStatus());
        dto.setTestStatus(tool.getTestStatus());
        dto.setLastTestAt(tool.getLastTestAt());
        dto.setInputParams(readParams(tool.getInputParamsJson()));
        dto.setOutputParams(readParams(tool.getOutputParamsJson()));
        dto.setRequestExample(tool.getRequestExample());
        dto.setResponseExample(tool.getResponseExample());
        return dto;
    }

    private List<PluginHeaderDTO> readHeaders(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PluginHeaderDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<PluginParamDTO> readParams(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<PluginParamDTO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private void applyRequest(Plugin plugin, PluginCreateRequest request) {
        plugin.setName(request.getName());
        plugin.setIntro(request.getIntro());
        plugin.setDescription(request.getDescription());
        plugin.setIconUrl(request.getIconUrl());
        plugin.setPluginType(request.getPluginType());
        plugin.setCreateMode(request.getCreateMode());
        plugin.setVersion(request.getVersion());
        plugin.setPluginUrl(request.getPluginUrl());
        plugin.setAuthType(request.getAuthType());
        plugin.setSpecJson(request.getSpecJson());
        plugin.setHeadersJson(writeJson(request.getHeaders()));
    }

    private void applyRequest(Plugin plugin, PluginUpdateRequest request) {
        plugin.setName(request.getName());
        plugin.setIntro(request.getIntro());
        plugin.setDescription(request.getDescription());
        plugin.setIconUrl(request.getIconUrl());
        plugin.setPluginType(request.getPluginType());
        plugin.setCreateMode(request.getCreateMode());
        plugin.setVersion(request.getVersion());
        plugin.setPluginUrl(request.getPluginUrl());
        plugin.setAuthType(request.getAuthType());
        plugin.setSpecJson(request.getSpecJson());
        plugin.setHeadersJson(writeJson(request.getHeaders()));
    }

    private void saveTools(Long pluginId, List<PluginToolDTO> toolDTOs) {
        if (toolDTOs == null) {
            return;
        }
        for (PluginToolDTO dto : toolDTOs) {
            PluginTool tool = new PluginTool();
            tool.setPluginId(pluginId);
            tool.setName(dto.getName());
            tool.setDescription(dto.getDescription());
            tool.setMethod(dto.getMethod());
            tool.setEndpoint(dto.getEndpoint());
            tool.setServiceStatus(Optional.ofNullable(dto.getServiceStatus()).orElse("offline"));
            tool.setTestStatus(Optional.ofNullable(dto.getTestStatus()).orElse("pending"));
            tool.setInputParamsJson(writeJson(dto.getInputParams()));
            tool.setOutputParamsJson(writeJson(dto.getOutputParams()));
            tool.setRequestExample(dto.getRequestExample());
            tool.setResponseExample(dto.getResponseExample());
            pluginToolMapper.insert(tool);
        }
    }

    private String writeJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    private Plugin requirePlugin(Long pluginId) {
        Plugin plugin = pluginMapper.selectById(pluginId);
        if (plugin == null) {
            throw new RuntimeException("插件不存在");
        }
        return plugin;
    }

    @Override
    public List<String> listPublishedNames() {
        return pluginMapper.selectPublishedNames();
    }
}


