// src/main/java/com/example/demo/app/mapper/UserLoginLogMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserLoginLogMapper {

    int insert(UserLoginLog userLoginLog);

    UserLoginLog selectById(@Param("id") Long id);

    List<UserLoginLog> selectByUserId(@Param("userId") Long userId);

    List<UserLoginLog> selectByUserIdAndLimit(@Param("userId") Long userId, @Param("limit") int limit);

    List<UserLoginLog> selectByUserIdAndTime(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    List<UserLoginLog> selectByIpAddress(@Param("ipAddress") String ipAddress);

    List<UserLoginLog> selectByStatus(@Param("status") Integer status);

    int countLoginAttempts(@Param("userId") Long userId, @Param("startTime") LocalDateTime startTime);

    int deleteOldLogs(@Param("beforeTime") LocalDateTime beforeTime);
}