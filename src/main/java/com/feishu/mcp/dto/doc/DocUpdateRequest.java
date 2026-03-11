package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文档更新请求 DTO
 */
@Data
@Builder
public class DocUpdateRequest {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 更新操作列表
     */
    private List<DocBlockUpdate> requests;

    /**
     * 文档版本号（可选）
     */
    private Long revisionId;
}
