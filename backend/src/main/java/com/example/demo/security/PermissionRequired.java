// src/main/java/com/example/demo/security/PermissionRequired.java
package com.example.demo.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PermissionRequired {
    String value() default "";
}