package com.feishu.mcp.dto.doc;

import com.feishu.mcp.constant.DocBlockType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文档块 DTO
 */
@Data
@Builder
public class DocBlock {

    /**
     * 块 ID
     */
    private String blockId;

    /**
     * 父块 ID
     */
    private String parentId;

    /**
     * 块类型
     */
    private DocBlockType blockType;

    /**
     * 块类型代码
     */
    private Integer blockTypeCode;

    /**
     * 子块数量
     */
    private Integer childrenCount;

    /**
     * 子块列表
     */
    private List<DocBlock> children;

    /**
     * 块内容（文本、标题等）
     */
    private String content;

    /**
     * 是否可编辑
     */
    public boolean isEditable() {
        return blockType != null && blockType.isTextEditable();
    }

    /**
     * 是否为页面块
     */
    public boolean isPage() {
        return blockType == DocBlockType.PAGE;
    }

    /**
     * 是否为标题块
     */
    public boolean isHeading() {
        return blockType != null && blockType.isHeading();
    }
}
