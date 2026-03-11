package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 文档更新响应 DTO
 */
@Data
@Builder
public class DocUpdateResponse {

    /**
     * 是否更新成功
     */
    private boolean success;

    /**
     * 更新后的文档版本号
     */
    private Long revisionId;

    /**
     * 更新的块ID
     */
    private String updatedBlockId;

    /**
     * 块索引（如果使用索引定位）
     */
    private Integer blockIndex;

    /**
     * 操作类型
     */
    private String operation;

    /**
     * 错误消息（失败时）
     */
    private String errorMessage;

    /**
     * 原始响应内容
     */
    private String rawResponse;

    /**
     * 创建成功响应
     */
    public static DocUpdateResponse success(String blockId, String operation, Long revisionId) {
        return DocUpdateResponse.builder()
                .success(true)
                .updatedBlockId(blockId)
                .operation(operation)
                .revisionId(revisionId)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static DocUpdateResponse error(String message, String rawResponse) {
        return DocUpdateResponse.builder()
                .success(false)
                .errorMessage(message)
                .rawResponse(rawResponse)
                .build();
    }
}
