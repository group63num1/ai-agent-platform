package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.*;
import com.example.demo.service.PluginService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {

    @Autowired
    private PluginService pluginService;

    @Autowired
    private JwtUtil jwtUtil;

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
                                         HttpServletRequest httpRequest) {
        try {
            Long userId = requireUserId(httpRequest);
            return ApiResponse.ok(pluginService.createPlugin(userId, request));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(401, e.getMessage());
        }
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

    private Long requireUserId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        String bearer = request == null ? null : request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    return userId;
                }
            }
        }
        throw new IllegalStateException("未登录或token无效");
    }
}





