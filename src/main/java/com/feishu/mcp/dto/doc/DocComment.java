package com.feishu.mcp.dto.doc;

import lombok.Data;

/**
 * 文档评论 DTO
 */
@Data
public class DocComment {

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 创建者名称
     */
    private String createdByName;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 是否根评论
     */
    private boolean root;

    /**
     * 引用的文本（划词评论）
     */
    private String quote;

    /**
     * 父评论ID（回复时使用）
     */
    private String parentId;
}
