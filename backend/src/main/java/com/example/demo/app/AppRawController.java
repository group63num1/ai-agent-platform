package com.example.demo.app;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/apps-raw")
public class AppRawController {

    // 成功：直接返回数组（不是统一响应）
    @GetMapping
    public List<Map<String, Object>> list() {
        List<Map<String, Object>> out = new ArrayList<>();
        Map<String, Object> a = new HashMap<>();
        a.put("id", 1);
        a.put("name", "智能客服助手");
        out.add(a);
        return out;
    }

    // 失败：故意抛错（默认异常响应）
    @GetMapping("/boom")
    public Object boom() {
        throw new RuntimeException("演示：服务器炸了");
    }
}

