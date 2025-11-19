// src/main/java/com/example/demo/app/mapper/KnowledgeChunkEmbeddingMapper.java
package com.example.demo.app.mapper;

import com.example.demo.app.entity.KnowledgeChunkEmbedding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface KnowledgeChunkEmbeddingMapper {

    int insert(KnowledgeChunkEmbedding embedding);

    int insertBatch(@Param("embeddings") List<KnowledgeChunkEmbedding> embeddings);

    KnowledgeChunkEmbedding selectById(@Param("id") Long id);

    KnowledgeChunkEmbedding selectByChunkIdAndModel(@Param("chunkId") Long chunkId, @Param("model") String model);

    List<KnowledgeChunkEmbedding> selectByChunkId(@Param("chunkId") Long chunkId);

    int deleteByChunkId(@Param("chunkId") Long chunkId);
}

