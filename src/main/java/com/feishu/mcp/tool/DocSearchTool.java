package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 文档搜索工具 - 搜索云文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocSearchTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_search";
    }

    @Override
    public String getDescription() {
        return "根据关键词、创建者等条件，在飞书云文档中进行精确或模糊搜索";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("query", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "搜索关键词"));
        properties.set("creator", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "创建者用户ID（可选）"));
        properties.set("page_size", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "每页返回数量，默认10")
                .put("default", 10));
        properties.set("page", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "页码，从1开始")
                .put("default", 1));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("query"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String query = parameters.has("query") ? parameters.get("query").asText() : "";
        String creator = parameters.has("creator") ? parameters.get("creator").asText() : null;
        int pageSize = parameters.has("page_size") ? parameters.get("page_size").asInt() : 10;
        int page = parameters.has("page") ? parameters.get("page").asInt() : 1;

        List<Map<String, Object>> docs = feishuDocService.searchDocs(query, creator, pageSize, page);

        ObjectNode result = objectMapper.createObjectNode();
        result.putPOJO("documents", docs);
        result.put("total", docs.size());
        result.put("query", query);

        return result;
    }
}