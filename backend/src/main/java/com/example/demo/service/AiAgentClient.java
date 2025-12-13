package com.example.demo.service;

import com.example.demo.config.AiAgentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class AiAgentClient {

    private static final String DONE_TOKEN = "[DONE]";
    private static final String CONTENT_FIELD = "content";

    private final AiAgentConfig config;
    private final ObjectMapper objectMapper;

    public AiAgentClient(AiAgentConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public void streamChat(Map<String, Object> payload, Consumer<String> eventConsumer) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(config.getChatUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(config.getConnectTimeoutMs());
            connection.setReadTimeout(config.getReadTimeoutMs());
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "text/event-stream");

            byte[] body = objectMapper.writeValueAsBytes(payload);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(body);
                os.flush();
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                String errorMessage = readStream(connection.getErrorStream());
                throw new IOException("调用 AI Agent 失败，状态码=" + status + ", body=" + errorMessage);
            }

            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder eventBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        if (eventBuilder.length() > 0) {
                            eventBuilder.append('\n');
                        }
                        eventBuilder.append(line.substring(5).trim());
                    } else if (line.isEmpty()) {
                        dispatchEvent(eventBuilder, eventConsumer);
                    }
                }
                dispatchEvent(eventBuilder, eventConsumer);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 同步调用 AI Agent /api/chat（底层仍走SSE流），并拼接返回的assistant内容。
     */
    public String chatOnce(Map<String, Object> payload) throws IOException {
        StringBuilder assistant = new StringBuilder();
        streamChat(payload, event -> {
            if (DONE_TOKEN.equals(event)) {
                return;
            }
            appendAssistantContent(event, assistant);
        });
        return assistant.toString();
    }

    private void dispatchEvent(StringBuilder eventBuilder, Consumer<String> eventConsumer) throws IOException {
        if (eventBuilder.length() == 0) {
            return;
        }
        String event = eventBuilder.toString();
        eventBuilder.setLength(0);
        eventConsumer.accept(event);
        if (DONE_TOKEN.equals(event)) {
            // 结束事件会由上层处理，直接返回
            return;
        }
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    private void appendAssistantContent(String event, StringBuilder buffer) {
        if (buffer == null || event == null || event.trim().isEmpty() || DONE_TOKEN.equals(event)) {
            return;
        }
        try {
            var node = objectMapper.readTree(event);
            var contentNode = node.get(CONTENT_FIELD);
            if (contentNode != null && !contentNode.isNull()) {
                buffer.append(contentNode.asText());
            }
        } catch (Exception ignore) {
            // 非JSON片段时忽略
        }
    }
}






