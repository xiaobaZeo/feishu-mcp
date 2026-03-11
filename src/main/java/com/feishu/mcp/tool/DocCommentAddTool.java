package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.dto.doc.DocComment;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文档评论添加工具 - 添加文档评论
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocCommentAddTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_comment_add";
    }

    @Override
    public String getDescription() {
        return "在指定云文档中添加文档评论，可以是全文评论或划词评论";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID"));
        properties.set("content", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "评论内容"));
        properties.set("quote", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "划词评论时，需要传入被评论的内容"));

        schema.set("properties", properties);
        ArrayNode required = objectMapper.createArrayNode();
        required.add("document_id");
        required.add("content");
        schema.set("required", required);
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();
        String content = parameters.get("content").asText();
        String quote = parameters.has("quote") ? parameters.get("quote").asText() : null;

        DocComment result = feishuDocService.addDocComment(documentId, content, quote);

        return objectMapper.valueToTree(result);
    }
}