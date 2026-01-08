package com.example.demo.service;

import com.example.demo.app.entity.Plugin;
import com.example.demo.app.entity.PluginTool;
import com.example.demo.app.mapper.PluginMapper;
import com.example.demo.app.mapper.PluginToolMapper;
import com.example.demo.util.UserScopedNameUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 将 Agent 中保存的“插件名称列表”解析为 aiagent 需要的 “工具名称列表（带 userId_ 前缀）”。
 * 不改变数据库中保存的原始名称，仅用于构造发往 aiagent 的请求。
 */
@Service
public class ToolResolverService {

    @Autowired
    private PluginMapper pluginMapper;

    @Autowired
    private PluginToolMapper pluginToolMapper;

    /**
     * 输入：插件名称列表（例如 ["天气查询"]）
     * 输出：工具名称列表（例如 ["12_getWeatherInfo", "12_getWeatherForecast"]）
     */
    public List<String> resolveTools(Long userId, List<String> pluginNames) {
        if (userId == null || pluginNames == null || pluginNames.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> toolIds = new LinkedHashSet<>();
        for (String pluginName : pluginNames) {
            if (pluginName == null || pluginName.trim().isEmpty()) {
                continue;
            }
            Plugin plugin = pluginMapper.selectByUserIdAndName(userId, pluginName.trim());
            if (plugin == null || plugin.getId() == null) {
                continue;
            }
            List<PluginTool> tools = pluginToolMapper.selectByPluginId(plugin.getId());
            if (tools == null || tools.isEmpty()) {
                continue;
            }
            for (PluginTool tool : tools) {
                if (tool == null || tool.getName() == null || tool.getName().trim().isEmpty()) {
                    continue;
                }
                toolIds.add(UserScopedNameUtil.prefix(userId, tool.getName().trim()));
            }
        }
        return new ArrayList<>(toolIds);
    }
}


