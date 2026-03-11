package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 创建文档请求 DTO
 */
@Data
@Builder
public class DocCreateRequest {

    /**
     * 知识空间节点ID（可选，为空则创建在我的文档库）
     */
    private String nodeId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档内容（纯文本，可选）
     */
    private String content;

    /**
     * 结构化内容块（可选，优先级高于 content）
     */
    private List<List<DocElement>> contentBlocks;

    /**
     * 创建带有纯文本内容的请求
     */
    public static DocCreateRequest withTextContent(String title, String content) {
        return DocCreateRequest.builder()
                .title(title)
                .content(content)
                .build();
    }

    /**
     * 创建带有结构化内容的请求
     */
    public static DocCreateRequest withBlocks(String title, List<List<DocElement>> blocks) {
        return DocCreateRequest.builder()
                .title(title)
                .contentBlocks(blocks)
                .build();
    }

    /**
     * 创建在知识空间的请求
     */
    public static DocCreateRequest inNode(String nodeId, String title, String content) {
        return DocCreateRequest.builder()
                .nodeId(nodeId)
                .title(title)
                .content(content)
                .build();
    }
}
