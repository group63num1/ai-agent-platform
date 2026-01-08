// src/main/java/com/example/demo/app/mapper/KnowledgeChunkMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {

    int insert(KnowledgeChunk chunk);

    int insertBatch(@Param("chunks") List<KnowledgeChunk> chunks);

    KnowledgeChunk selectById(@Param("id") Long id);

    List<KnowledgeChunk> selectByDocumentId(@Param("documentId") Long documentId);

    List<KnowledgeChunk> selectByIds(@Param("ids") List<Long> ids);

    int deleteByDocumentId(@Param("documentId") Long documentId);

    int countByDocumentId(@Param("documentId") Long documentId);
}

