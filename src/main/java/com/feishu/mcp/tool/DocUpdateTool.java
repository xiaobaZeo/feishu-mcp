package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档更新工具 - 更新云文档
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocUpdateTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_update";
    }

    @Override
    public String getDescription() {
        return "在云文档指定位置增加内容，也可完善或替换指定内容";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID"));
        properties.set("operation", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "操作类型：insert（插入）或 replace（替换）")
                .put("enum", objectMapper.createArrayNode().add("insert").add("replace")));
        properties.set("text", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "要插入或替换的文本内容"));
        properties.set("start_index", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "起始位置索引（从0开始）"));
        properties.set("end_index", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "结束位置索引（仅replace操作需要）"));

        schema.set("properties", properties);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("document_id");
        required.add("operation");
        required.add("text");
        schema.set("required", required);
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();
        String operation = parameters.get("operation").asText();
        String text = parameters.get("text").asText();

        List<Map<String, Object>> requests = new ArrayList<>();

        if ("replace".equals(operation)) {
            // 替换操作需要指定范围
            int startIndex = parameters.has("start_index") ? parameters.get("start_index").asInt() : 0;
            int endIndex = parameters.has("end_index") ? parameters.get("end_index").asInt() : startIndex;

            Map<String, Object> request = new HashMap<>();
            request.put("type", "replace");
            request.put("start_index", startIndex);
            request.put("end_index", endIndex);
            request.put("text", text);
            requests.add(request);
        } else {
            // 插入操作
            int startIndex = parameters.has("start_index") ? parameters.get("start_index").asInt() : 0;

            Map<String, Object> request = new HashMap<>();
            request.put("type", "insert");
            request.put("start_index", startIndex);
            request.put("end_index", startIndex);
            request.put("text", text);
            requests.add(request);
        }

        Map<String, Object> result = feishuDocService.updateDoc(documentId, requests);

        return objectMapper.valueToTree(result);
    }
}