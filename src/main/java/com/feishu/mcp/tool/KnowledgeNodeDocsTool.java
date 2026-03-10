package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuKnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 知识空间文档列表工具 - 获取知识空间节点下的文档列表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeNodeDocsTool implements McpTool {

    private final FeishuKnowledgeService feishuKnowledgeService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "knowledge_node_docs";
    }

    @Override
    public String getDescription() {
        return "获取指定知识空间节点下的云文档列表，支持分页";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("node_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "知识空间节点ID"));
        properties.set("page_size", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "每页返回数量，默认10")
                .put("default", 10));
        properties.set("page_token", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "分页token（用于获取下一页）"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("node_id"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String nodeId = parameters.get("node_id").asText();
        int pageSize = parameters.has("page_size") ? parameters.get("page_size").asInt() : 10;
        String pageToken = parameters.has("page_token") ? parameters.get("page_token").asText() : null;

        Map<String, Object> result = feishuKnowledgeService.getNodeDocuments(nodeId, pageSize, pageToken);

        return objectMapper.valueToTree(result);
    }
}