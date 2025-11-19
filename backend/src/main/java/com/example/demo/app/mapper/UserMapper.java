// src/main/java/com/example/demo/app/mapper/UserMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    int insert(User user);

    User selectById(@Param("id") Long id);

    User selectByUsername(@Param("username") String username);

    User selectByEmail(@Param("email") String email);

    User selectByPhone(@Param("phone") String phone);



    List<User> selectAll();

    int update(User user);

    int softDelete(@Param("id") Long id);

    int hardDelete(@Param("id") Long id);

    int updateLoginInfo(@Param("id") Long id,
                        @Param("lastLoginAt") LocalDateTime lastLoginAt,
                        @Param("lastLoginIp") String lastLoginIp);

    int updateLoginAttempts(@Param("id") Long id,
                            @Param("loginAttempts") Integer loginAttempts,
                            @Param("lockedUntil") LocalDateTime lockedUntil);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateEmailVerified(@Param("id") Long id, @Param("emailVerified") Integer emailVerified);

    int updatePhoneVerified(@Param("id") Long id, @Param("phoneVerified") Integer phoneVerified);
}