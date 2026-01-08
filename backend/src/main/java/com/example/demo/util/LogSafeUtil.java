package com.example.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日志安全输出工具：做 JSON 序列化失败兜底、字符串截断，避免日志过大或因异常中断业务。
 */
public final class LogSafeUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LogSafeUtil() {}

    public static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    public static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        if (maxLen <= 0) {
            return "";
        }
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen) + "...(truncated,len=" + value.length() + ")";
    }

    public static Map<String, Object> fileContentPreview(String filename, String content, int previewChars) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("filename", filename);
        m.put("content_len", content == null ? 0 : content.length());
        m.put("content_preview", truncate(content, previewChars));
        return m;
    }
}


