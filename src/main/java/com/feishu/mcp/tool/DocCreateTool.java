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
 * 文档创建工具 - 创建云文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocCreateTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_create";
    }

    @Override
    public String getDescription() {
        return "在飞书云文档中创建新的云文档。可在\"我的文档库\"或指定知识空间节点下创建。";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("title", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档标题"));
        properties.set("node_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "知识空间节点ID（可选，为空则创建在我的文档库）"));
        properties.set("content", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档初始内容（可选）"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("title"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String title = parameters.get("title").asText();
        String nodeId = parameters.has("node_id") ? parameters.get("node_id").asText() : null;
        String content = parameters.has("content") ? parameters.get("content").asText() : null;

        Map<String, Object> result = feishuDocService.createDoc(nodeId, title, content);

        return objectMapper.valueToTree(result);
    }
}