package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.dto.doc.DocComment;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档评论列表工具 - 查看云文档的评论
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocCommentListTool implements McpTool {

    private final FeishuDocService feishuDocService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "doc_comment_list";
    }

    @Override
    public String getDescription() {
        return "查看指定云文档中的全文评论和划词评论";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("document_id"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String documentId = parameters.get("document_id").asText();

        List<DocComment> comments = feishuDocService.getDocComments(documentId);

        ObjectNode result = objectMapper.createObjectNode();
        result.putPOJO("comments", comments);
        result.put("total", comments.size());

        return result;
    }
}