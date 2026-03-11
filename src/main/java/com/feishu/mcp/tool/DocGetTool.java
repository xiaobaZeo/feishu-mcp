package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.dto.doc.DocBlock;
import com.feishu.mcp.dto.doc.DocContent;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import com.feishu.mcp.util.FeishuUrlParser;
import com.feishu.mcp.util.SchemaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档获取工具 - 查看云文档内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocGetTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_get";
    }

    @Override
    public String getDescription() {
        return "根据文档链接或文档ID，获取云文档的完整内容。支持从完整的飞书文档 URL 中提取文档ID。";
    }

    @Override
    public JsonNode getInputSchema() {
        return new SchemaBuilder(objectMapper)
                .addString("document_id",
                        "文档ID（可以直接使用ID，或传入飞书文档URL，如 https://xxx.feishu.cn/wiki/XXXX 或 https://xxx.feishu.cn/docx/XXXX）",
                        true)
                .addBoolean("include_blocks", "是否包含块内容详情", false, true)
                .build();
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        // 支持从 URL 解析文档ID
        String documentId = FeishuUrlParser.extractDocumentId(parameters.get("document_id").asText());
        boolean includeBlocks = getBoolean(parameters, "include_blocks", true);

        log.info("获取文档内容: document_id={}", documentId);

        DocContent content;
        if (includeBlocks) {
            content = feishuDocService.getDocWithBlocks(documentId);
        } else {
            content = feishuDocService.getDoc(documentId);
        }

        // 构建友好的响应格式
        DocGetResult result = new DocGetResult();
        result.setDocumentId(content.getDocumentId());
        result.setTitle(content.getTitle());
        result.setRevision(content.getRevision());

        // 格式化块内容
        if (content.getBlocks() != null) {
            result.setBlocks(formatBlocks(content.getBlocks()));
            result.setPlainText(content.getPlainText());
        }

        return objectMapper.valueToTree(result);
    }

    private String formatBlocks(List<DocBlock> blocks) {
        StringBuilder sb = new StringBuilder();
        int editableIndex = 0;

        for (int i = 0; i < blocks.size(); i++) {
            DocBlock block = blocks.get(i);

            // 跳过 page 块
            if (block.isPage()) {
                continue;
            }

            sb.append("[").append(editableIndex).append("] ");
            sb.append("[").append(block.getBlockType().getDescription()).append("] ");

            if (block.getContent() != null && !block.getContent().isEmpty()) {
                sb.append(block.getContent());
            } else {
                sb.append("(空)");
            }

            sb.append("\n");
            editableIndex++;
        }

        return sb.toString().trim();
    }

    private boolean getBoolean(JsonNode node, String field, boolean defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asBoolean() : defaultValue;
    }

    // 响应 DTO
    public static class DocGetResult {
        private String documentId;
        private String title;
        private Long revision;
        private String plainText;
        private String blocks;

        // Getters and Setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Long getRevision() { return revision; }
        public void setRevision(Long revision) { this.revision = revision; }
        public String getPlainText() { return plainText; }
        public void setPlainText(String plainText) { this.plainText = plainText; }
        public String getBlocks() { return blocks; }
        public void setBlocks(String blocks) { this.blocks = blocks; }
    }
}
