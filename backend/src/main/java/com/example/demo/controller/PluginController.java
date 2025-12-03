package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.PluginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    @Autowired
    private PluginService pluginService;

    @GetMapping
    public ApiResponse<List<PluginSummaryDTO>> list() {
        return ApiResponse.ok(pluginService.listPlugins());
    }

    @GetMapping("/{id}")
    public ApiResponse<PluginDTO> detail(@PathVariable Long id) {
        return ApiResponse.ok(pluginService.getPluginDetail(id));
    }

    @PostMapping
    public ApiResponse<PluginDTO> create(@RequestBody PluginCreateRequest request,
                                         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Long safeUserId = userId != null ? userId : 0L;
        return ApiResponse.ok(pluginService.createPlugin(safeUserId, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PluginDTO> update(@PathVariable Long id, @RequestBody PluginUpdateRequest request) {
        return ApiResponse.ok(pluginService.updatePlugin(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        pluginService.deletePlugin(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/toggle")
    public ApiResponse<PluginDTO> toggle(@PathVariable Long id, @RequestParam boolean enable) {
        return ApiResponse.ok(pluginService.togglePlugin(id, enable));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<PluginDTO> publish(@PathVariable Long id) {
        return ApiResponse.ok(pluginService.publishPlugin(id));
    }

    @PostMapping("/test")
    public ApiResponse<PluginTestResultDTO> test(@RequestBody PluginTestRequest request) {
        return ApiResponse.ok(pluginService.testPluginTool(request));
    }

    @PostMapping("/import")
    public ApiResponse<PluginTemplateDTO> importTemplate(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(pluginService.importTemplate(file));
    }

    @GetMapping("/getlist")
    public ApiResponse<List<String>> listPublishedNames() {
        return ApiResponse.ok(pluginService.listPublishedNames());
    }
}





