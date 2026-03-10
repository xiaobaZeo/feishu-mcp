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
        return "根据文档链接或文档ID，获取云文档内的完整内容";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID（可以从文档链接中提取，例如 https://open.feishu.com/document/XXXX 中的XXXX部分）"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("document_id"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();

        Map<String, Object> docInfo = feishuDocService.getDoc(documentId);

        return objectMapper.valueToTree(docInfo);
    }
}