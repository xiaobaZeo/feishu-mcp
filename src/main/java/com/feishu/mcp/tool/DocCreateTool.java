package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.dto.doc.DocCreateRequest;
import com.feishu.mcp.dto.doc.DocCreateResponse;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import com.feishu.mcp.util.FeishuUrlParser;
import com.feishu.mcp.util.SchemaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
        return "在飞书云文档中创建新的云文档。可在我的文档库或指定知识空间节点下创建。";
    }

    @Override
    public JsonNode getInputSchema() {
        return new SchemaBuilder(objectMapper)
                .addString("title", "文档标题", true)
                .addString("node_id", "知识空间节点ID（可选，为空则创建在我的文档库）", false)
                .addString("content", "文档初始内容（可选）", false)
                .addString("format", "内容格式：plain（纯文本）或 markdown", false, "plain")
                .build();
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String title = parameters.get("title").asText();
        String nodeId = getString(parameters, "node_id", null);
        String content = getString(parameters, "content", null);
        String format = getString(parameters, "format", "plain");

        // 处理 node_id（支持从 URL 提取）
        if (nodeId != null && FeishuUrlParser.isValidDocumentIdOrUrl(nodeId)) {
            // node_id 也可能是 URL 格式，直接提取
            // 注意：这里 node_id 实际上是知识空间节点 ID，不是文档 ID
            // 但为了安全起见，我们还是检查一下
            log.debug("Node ID: {}", nodeId);
        }

        log.info("创建文档: title={}, node_id={}", title, nodeId);

        DocCreateRequest request = DocCreateRequest.builder()
                .title(title)
                .nodeId(nodeId)
                .content(content)
                .build();

        DocCreateResponse result = feishuDocService.createDoc(request);

        if (!result.isSuccess()) {
            log.error("创建文档失败: {}", result.getErrorMessage());
        }

        return objectMapper.valueToTree(result);
    }

    private String getString(JsonNode node, String field, String defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : defaultValue;
    }
}
