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

    private final AiAgentConfig config;
    private final ObjectMapper objectMapper;

    public AiAgentClient(AiAgentConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    public void streamChat(Map<String, Object> payload, Consumer<String> eventConsumer) throws IOException {
        HttpURLConnection connection = null;
        try {
            String chatUrl = config.getChatUrl();
	    System.out.println("AI Agent Chat URL: " + chatUrl);
	    URL url = new URL(chatUrl);
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
}




