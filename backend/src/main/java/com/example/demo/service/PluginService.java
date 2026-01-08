package com.example.demo.service;

import com.example.demo.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PluginService {
    List<PluginSummaryDTO> listPlugins();

    PluginDTO getPluginDetail(Long pluginId);

    PluginDTO createPlugin(Long userId, PluginCreateRequest request);

    PluginDTO updatePlugin(Long pluginId, PluginUpdateRequest request);

    void deletePlugin(Long pluginId);

    PluginDTO togglePlugin(Long pluginId, boolean enable);

    PluginDTO publishPlugin(Long pluginId);

    PluginTestResultDTO testPluginTool(PluginTestRequest request);

    PluginTemplateDTO importTemplate(MultipartFile file);

    List<String> listPublishedNames();
}





