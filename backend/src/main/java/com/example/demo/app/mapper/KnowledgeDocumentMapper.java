// src/main/java/com/example/demo/app/mapper/KnowledgeDocumentMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper {

    int insert(KnowledgeDocument document);

    KnowledgeDocument selectById(@Param("id") Long id);

    List<KnowledgeDocument> selectByUserId(@Param("userId") Long userId, @Param("includeDeleted") Boolean includeDeleted);

    int update(KnowledgeDocument document);

    int softDelete(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("errorMessage") String errorMessage);

    int updateChunkCount(@Param("id") Long id, @Param("chunkCount") Integer chunkCount);
}

