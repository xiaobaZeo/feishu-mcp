package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 文档清空响应 DTO
 */
@Data
@Builder
public class DocClearResponse {

    /**
     * 是否清空成功
     */
    private boolean success;

    /**
     * 删除的内容块数量
     */
    private int deletedCount;

    /**
     * Page 块ID
     */
    private String pageBlockId;

    /**
     * 删除前的子块数量
     */
    private int childrenCount;

    /**
     * 文档版本号
     */
    private Long revisionId;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;

    /**
     * 创建空文档响应（无需清空）
     */
    public static DocClearResponse empty() {
        return DocClearResponse.builder()
                .success(true)
                .deletedCount(0)
                .childrenCount(0)
                .build();
    }

    /**
     * 创建成功响应
     */
    public static DocClearResponse success(int deletedCount, String pageBlockId, Long revisionId) {
        return DocClearResponse.builder()
                .success(true)
                .deletedCount(deletedCount)
                .pageBlockId(pageBlockId)
                .revisionId(revisionId)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static DocClearResponse error(String message) {
        return DocClearResponse.builder()
                .success(false)
                .errorMessage(message)
                .build();
    }
}
