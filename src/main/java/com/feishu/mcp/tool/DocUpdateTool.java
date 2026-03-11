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
        return "在云文档指定位置增加或替换内容。通过 block_index 指定第几个内容块（从0开始，0表示第一个内容块），使用 operation 指定是 insert（追加）还是 replace（替换）";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID（可以从文档链接中提取，例如 https://my.feishu.cn/wiki/XXXX 中的XXXX部分）"));
        properties.set("text", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "要插入或替换的文本内容"));
        properties.set("operation", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "操作类型：insert（在现有内容后追加）或 replace（替换现有内容）")
                .put("enum", objectMapper.createArrayNode().add("insert").add("replace")));
        properties.set("block_index", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "内容块索引（从0开始，0表示第一个可编辑内容块，以此类推）"));

        schema.set("properties", properties);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("document_id");
        required.add("text");
        required.add("operation");
        schema.set("required", required);
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();
        String text = parameters.get("text").asText();
        String operation = parameters.has("operation") ? parameters.get("operation").asText() : "replace";
        int blockIndex = parameters.has("block_index") ? parameters.get("block_index").asInt() : 0;

        log.info("更新文档内容: document_id={}, operation={}, block_index={}, text={}",
                documentId, operation, blockIndex, text);
        Map<String, Object> result = feishuDocService.updateDocContent(documentId, text, operation, blockIndex);

        return objectMapper.valueToTree(result);
    }
}