package com.feishu.mcp.dto.doc;

import lombok.Data;

/**
 * 文档搜索结果 DTO
 */
@Data
public class DocSearchResult {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型：docx（云文档）、sheet（表格）等
     */
    private String docType;

    /**
     * 文档链接
     */
    private String url;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 所属知识空间
     */
    private String spaceName;

    /**
     * 知识空间节点ID
     */
    private String nodeId;
}
