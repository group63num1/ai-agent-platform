// src/main/java/com/example/demo/util/TextChunkUtil.java
package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 文本分片工具类
 * 将长文本分割成适合LLM处理的块
 */
public class TextChunkUtil {

    // 默认块大小（字符数）
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    
    // 默认重叠大小（字符数）
    private static final int DEFAULT_OVERLAP_SIZE = 200;
    
    // 段落分隔符
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\n\n+");
    
    // 句子分隔符
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[.!?。！？]\\s*");

    /**
     * 将文本分割成块
     * 
     * @param text 原始文本
     * @param chunkSize 每个块的最大字符数
     * @param overlapSize 块之间的重叠字符数
     * @return 文本块列表
     */
    public static List<String> chunkText(String text, int chunkSize, int overlapSize) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> chunks = new ArrayList<>();
        
        // 如果文本长度小于块大小，直接返回
        if (text.length() <= chunkSize) {
            chunks.add(text.trim());
            return chunks;
        }

        // 尝试按段落分割
        String[] paragraphs = PARAGRAPH_PATTERN.split(text);
        List<String> paragraphChunks = new ArrayList<>();
        
        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) {
                continue;
            }
            
            // 如果段落长度小于块大小，直接添加
            if (paragraph.length() <= chunkSize) {
                paragraphChunks.add(paragraph);
            } else {
                // 段落太长，按句子分割
                List<String> sentenceChunks = chunkBySentence(paragraph, chunkSize, overlapSize);
                paragraphChunks.addAll(sentenceChunks);
            }
        }

        // 合并相邻的小块
        chunks = mergeChunks(paragraphChunks, chunkSize, overlapSize);

        return chunks;
    }

    /**
     * 按句子分割文本
     */
    private static List<String> chunkBySentence(String text, int chunkSize, int overlapSize) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = SENTENCE_PATTERN.split(text);
        
        StringBuilder currentChunk = new StringBuilder();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) {
                continue;
            }
            
            // 如果当前块加上新句子超过大小，保存当前块并开始新块
            if (currentChunk.length() > 0 && 
                currentChunk.length() + sentence.length() + 1 > chunkSize) {
                chunks.add(currentChunk.toString().trim());
                
                // 重叠处理：保留当前块的末尾部分
                String overlapText = getOverlapText(currentChunk.toString(), overlapSize);
                currentChunk = new StringBuilder(overlapText);
            }
            
            if (currentChunk.length() > 0) {
                currentChunk.append(" ");
            }
            currentChunk.append(sentence);
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }

    /**
     * 合并相邻的小块
     */
    private static List<String> mergeChunks(List<String> chunks, int chunkSize, int overlapSize) {
        if (chunks.isEmpty()) {
            return chunks;
        }

        List<String> mergedChunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String chunk : chunks) {
            if (currentChunk.length() == 0) {
                currentChunk.append(chunk);
            } else if (currentChunk.length() + chunk.length() + 1 <= chunkSize) {
                // 可以合并
                currentChunk.append(" ").append(chunk);
            } else {
                // 不能合并，保存当前块
                mergedChunks.add(currentChunk.toString().trim());
                
                // 重叠处理：保留当前块的末尾部分
                String overlapText = getOverlapText(currentChunk.toString(), overlapSize);
                currentChunk = new StringBuilder(overlapText).append(" ").append(chunk);
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            mergedChunks.add(currentChunk.toString().trim());
        }
        
        return mergedChunks;
    }

    /**
     * 获取重叠文本（从末尾截取）
     */
    private static String getOverlapText(String text, int overlapSize) {
        if (text.length() <= overlapSize) {
            return text;
        }
        
        // 尝试在句子边界处截取
        int startIndex = Math.max(0, text.length() - overlapSize);
        String overlapText = text.substring(startIndex);
        
        // 找到第一个空格或标点符号，以便更好地分割
        int firstSpace = overlapText.indexOf(' ');
        if (firstSpace > 0 && firstSpace < overlapText.length() / 2) {
            overlapText = overlapText.substring(firstSpace + 1);
        }
        
        return overlapText.trim();
    }

    /**
     * 使用默认参数分割文本
     */
    public static List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP_SIZE);
    }

    /**
     * 估算token数量（简单估算：1 token ≈ 4个字符）
     */
    public static int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        // 简单估算：中文和英文混合，大约1 token = 3-4个字符
        return (int) Math.ceil(text.length() / 3.5);
    }
}

