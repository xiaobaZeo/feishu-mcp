package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 文档搜索请求 DTO
 */
@Data
@Builder
public class DocSearchRequest {

    /**
     * 搜索关键词
     */
    private String query;

    /**
     * 创建者用户ID
     */
    private String creator;

    /**
     * 每页返回数量，默认 10
     */
    @Builder.Default
    private int pageSize = 10;

    /**
     * 页码，从 1 开始，默认 1
     */
    @Builder.Default
    private int page = 1;

    /**
     * 搜索范围：all（全部）、mine（我的）
     */
    @Builder.Default
    private String scope = "all";
}
