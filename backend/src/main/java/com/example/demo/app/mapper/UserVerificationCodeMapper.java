// src/main/java/com/example/demo/app/mapper/UserVerificationCodeMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.UserVerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserVerificationCodeMapper {

    int insert(UserVerificationCode verificationCode);

    UserVerificationCode selectById(@Param("id") Long id);

    UserVerificationCode selectValidCode(@Param("target") String target,
                                         @Param("type") String type,
                                         @Param("code") String code);

    List<UserVerificationCode> selectByTargetAndType(@Param("target") String target,
                                                     @Param("type") String type);

    int markAsUsed(@Param("id") Long id);

    int deleteExpiredCodes(@Param("currentTime") LocalDateTime currentTime);

    int countRecentAttempts(@Param("target") String target,
                            @Param("type") String type,
                            @Param("sinceTime") LocalDateTime sinceTime);
}