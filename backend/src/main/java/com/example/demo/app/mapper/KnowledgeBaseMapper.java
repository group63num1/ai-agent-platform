package com.example.demo.app.mapper;

import com.example.demo.app.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper {
    int insert(KnowledgeBase kb);

    int update(KnowledgeBase kb);

    KnowledgeBase selectById(@Param("kbId") String kbId);

    int delete(@Param("kbId") String kbId);

    List<KnowledgeBase> selectPage(@Param("keyword") String keyword,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    int count(@Param("keyword") String keyword);

    /**
     * 获取已启用的知识库名称列表
     */
    List<String> selectEnabledNames();
}

