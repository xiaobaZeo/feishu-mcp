package com.feishu.mcp.dto.doc;

import com.feishu.mcp.constant.DocBlockType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 文档内容 DTO
 * 包含文档的基本信息和块内容
 */
@Data
@Builder
public class DocContent {

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档版本号
     */
    private Long revision;

    /**
     * 文档块列表
     */
    private List<DocBlock> blocks;

    /**
     * 原始响应内容（用于调试）
     */
    private String rawResponse;

    /**
     * 获取文档中的纯文本内容
     */
    public String getPlainText() {
        if (blocks == null || blocks.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (DocBlock block : blocks) {
            if (block.getContent() != null) {
                sb.append(block.getContent()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    /**
     * 获取可编辑的块列表
     */
    public List<DocBlock> getEditableBlocks() {
        if (blocks == null) {
            return List.of();
        }
        return blocks.stream()
                .filter(DocBlock::isEditable)
                .toList();
    }

    /**
     * 获取页面块
     */
    public DocBlock getPageBlock() {
        if (blocks == null) {
            return null;
        }
        return blocks.stream()
                .filter(DocBlock::isPage)
                .findFirst()
                .orElse(null);
    }
}
