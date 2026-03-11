package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.dto.doc.DocClearResponse;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import com.feishu.mcp.util.FeishuUrlParser;
import com.feishu.mcp.util.SchemaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文档清空工具 - 清空文档内容但保留文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocClearTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_clear";
    }

    @Override
    public String getDescription() {
        return "清空指定云文档的内容，保留文档本身。删除文档中所有文本、图片、表格等内容块，但保留文档标题和文档结构。支持从URL解析文档ID。";
    }

    @Override
    public JsonNode getInputSchema() {
        return new SchemaBuilder(objectMapper)
                .addString("document_id",
                        "文档ID（可以直接使用ID，或传入飞书文档URL，如 https://xxx.feishu.cn/wiki/XXXX）",
                        true)
                .build();
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        // 支持从 URL 解析文档ID
        String documentId = FeishuUrlParser.extractDocumentId(parameters.get("document_id").asText());

        log.info("清空文档: document_id={}", documentId);

        DocClearResponse result = feishuDocService.clearDoc(documentId);

        if (!result.isSuccess()) {
            log.error("清空文档失败: {}", result.getErrorMessage());
        } else {
            log.info("清空文档成功: deleted_count={}", result.getDeletedCount());
        }

        return objectMapper.valueToTree(result);
    }
}
