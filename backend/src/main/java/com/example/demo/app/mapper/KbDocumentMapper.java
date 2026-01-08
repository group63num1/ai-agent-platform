package com.example.demo.app.mapper;

import com.example.demo.app.entity.KbDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KbDocumentMapper {
    int insert(KbDocument doc);

    List<KbDocument> selectByKbId(@Param("kbId") String kbId);

    int deleteById(@Param("id") Long id);
}

