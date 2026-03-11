package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 创建文档响应 DTO
 */
@Data
@Builder
public class DocCreateResponse {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档 Token
     */
    private String token;

    /**
     * 文档链接
     */
    private String url;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 创建时间（时间戳）
     */
    private Long createTime;

    /**
     * 是否创建成功
     */
    private boolean success;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;
}
