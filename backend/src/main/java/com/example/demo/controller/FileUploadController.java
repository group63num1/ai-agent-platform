// src/main/java/com/example/demo/controller/FileUploadController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResponse;
import com.example.demo.dto.FileUploadResponseDTO;
import com.example.demo.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/upload")
public class FileUploadController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<FileUploadResponseDTO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            if (!fileUploadUtil.isImageFile(file)) {
                return ApiResponse.fail(400, "只能上传图片文件");
            }

            // 验证文件大小
            if (!fileUploadUtil.isFileSizeValid(file, MAX_FILE_SIZE)) {
                return ApiResponse.fail(400, "文件大小不能超过5MB");
            }

            // 上传文件
            String fileUrl = fileUploadUtil.uploadFile(file, "avatars");
            FileUploadResponseDTO response = new FileUploadResponseDTO(
                    fileUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ApiResponse.ok(response);
        } catch (IOException e) {
            return ApiResponse.fail(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 上传团队头像
     */
    @PostMapping("/team-avatar")
    public ApiResponse<FileUploadResponseDTO> uploadTeamAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            if (!fileUploadUtil.isImageFile(file)) {
                return ApiResponse.fail(400, "只能上传图片文件");
            }

            // 验证文件大小
            if (!fileUploadUtil.isFileSizeValid(file, MAX_FILE_SIZE)) {
                return ApiResponse.fail(400, "文件大小不能超过5MB");
            }

            // 上传文件
            String fileUrl = fileUploadUtil.uploadFile(file, "team-avatars");
            FileUploadResponseDTO response = new FileUploadResponseDTO(
                    fileUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ApiResponse.ok(response);
        } catch (IOException e) {
            return ApiResponse.fail(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 通用文件上传
     */
    @PostMapping("/file")
    public ApiResponse<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件大小
            if (!fileUploadUtil.isFileSizeValid(file, MAX_FILE_SIZE)) {
                return ApiResponse.fail(400, "文件大小不能超过5MB");
            }

            // 上传文件
            String fileUrl = fileUploadUtil.uploadFile(file, "files");
            FileUploadResponseDTO response = new FileUploadResponseDTO(
                    fileUrl,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            return ApiResponse.ok(response);
        } catch (IOException e) {
            return ApiResponse.fail(500, "文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping
    public ApiResponse<Void> deleteFile(@RequestParam String fileUrl) {
        try {
            boolean deleted = fileUploadUtil.deleteFile(fileUrl);
            if (deleted) {
                return new ApiResponse<>(0, "文件删除成功", null);
            } else {
                return ApiResponse.fail(400, "文件删除失败或文件不存在");
            }
        } catch (Exception e) {
            return ApiResponse.fail(400, e.getMessage());
        }
    }
}