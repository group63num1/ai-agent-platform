package com.example.demo.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 仅用于知识库模块链路追踪：为每个请求注入 requestId（rid）到 MDC，便于串联前端请求、AI请求/响应、前端响应。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String MDC_KEY_RID = "rid";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request == null ? null : request.getRequestURI();
        if (uri == null) {
            return true;
        }
        // 仅对知识库/工作流模块生效（明确忽略 /api/v1/knowledge）
        return !(uri.startsWith("/api/knowledge-bases") || uri.startsWith("/api/workflows"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER_REQUEST_ID);
        String rid = (incoming == null || incoming.trim().isEmpty())
                ? UUID.randomUUID().toString().replace("-", "")
                : incoming.trim();
        MDC.put(MDC_KEY_RID, rid);
        try {
            if (response != null) {
                response.setHeader(HEADER_REQUEST_ID, rid);
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY_RID);
        }
    }
}


