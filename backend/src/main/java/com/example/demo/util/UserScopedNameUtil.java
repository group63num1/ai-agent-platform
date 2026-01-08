package com.example.demo.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 将资源名称按 userId 做命名空间前缀化，例如: userId=1, name=pluginA -> 1_pluginA
 * 仅用于构造发往 aiagent 的请求，不改变数据库中保存的原始名称。
 */
public final class UserScopedNameUtil {

    private UserScopedNameUtil() {
    }

    public static String prefix(Long userId, String name) {
        if (userId == null || name == null) {
            return name;
        }
        String prefix = userId + "_";
        if (name.startsWith(prefix)) {
            return name;
        }
        return prefix + name;
    }

    public static List<String> prefixList(Long userId, List<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        return names.stream()
                .map(n -> prefix(userId, n))
                .collect(Collectors.toList());
    }
}


