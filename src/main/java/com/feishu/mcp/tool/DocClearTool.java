package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        return "清空指定云文档的内容，保留文档本身。删除文档中所有文本、图片、表格等内容块，但保留文档标题和文档结构。";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID（可以从文档链接中提取，例如 https://my.feishu.cn/wiki/XXXX 中的XXXX部分）"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("document_id"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();

        log.info("清空文档: document_id={}", documentId);
        Map<String, Object> result = feishuDocService.clearDoc(documentId);

        return objectMapper.valueToTree(result);
    }
}
