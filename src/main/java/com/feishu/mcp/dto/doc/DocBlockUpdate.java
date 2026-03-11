package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文档块更新 DTO
 * 表示单个块的更新操作
 */
@Data
@Builder
public class DocBlockUpdate {

    /**
     * 块ID
     */
    private String blockId;

    /**
     * 操作类型：update_text_elements, delete, insert, ...
     */
    private String operationType;

    /**
     * 新内容元素列表
     */
    private List<DocElement> elements;

    /**
     * 插入位置（insert 操作时使用）
     */
    private Integer insertIndex;

    /**
     * 创建文本内容更新
     */
    public static DocBlockUpdate updateText(String blockId, String text) {
        return DocBlockUpdate.builder()
                .blockId(blockId)
                .operationType("update_text_elements")
                .elements(List.of(DocElement.text(text)))
                .build();
    }

    /**
     * 创建删除块操作
     */
    public static DocBlockUpdate delete(String blockId) {
        return DocBlockUpdate.builder()
                .blockId(blockId)
                .operationType("delete")
                .build();
    }

    /**
     * 创建插入子块操作
     * @param parentBlockId 父块ID（通常是 page 块）
     * @param elements 内容元素列表
     * @param insertIndex 插入位置（0表示开头，-1或不传表示末尾）
     */
    public static DocBlockUpdate insertBlock(String parentBlockId, List<DocElement> elements, Integer insertIndex) {
        return DocBlockUpdate.builder()
                .blockId(parentBlockId)
                .operationType("insert_block_children")
                .elements(elements)
                .insertIndex(insertIndex)
                .build();
    }
}
