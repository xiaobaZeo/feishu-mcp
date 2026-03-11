package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.dto.doc.DocSearchRequest;
import com.feishu.mcp.dto.doc.DocSearchResult;
import com.feishu.mcp.constant.McpConstants;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import com.feishu.mcp.util.SchemaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

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
        return "根据关键词搜索飞书云文档，支持按创建者过滤";
    }

    @Override
    public JsonNode getInputSchema() {
        return new SchemaBuilder(objectMapper)
                .addString("query", "搜索关键词", true)
                .addString("creator", "创建者用户ID（可选）", false)
                .addInteger("page_size", "每页返回数量", false, McpConstants.DEFAULT_PAGE_SIZE)
                .addInteger("page", "页码，从1开始", false, McpConstants.DEFAULT_PAGE)
                .build();
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        // 构建请求
        DocSearchRequest request = DocSearchRequest.builder()
                .query(getString(parameters, "query", ""))
                .creator(getString(parameters, "creator", null))
                .pageSize(getInt(parameters, "page_size", McpConstants.DEFAULT_PAGE_SIZE))
                .page(getInt(parameters, "page", McpConstants.DEFAULT_PAGE))
                .build();

        log.info("搜索文档: query={}", request.getQuery());

        List<DocSearchResult> results = feishuDocService.searchDocs(request);

        // 构建响应
        return objectMapper.valueToTree(new SearchResultWrapper(results, request.getQuery()));
    }

    private String getString(JsonNode node, String field, String defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : defaultValue;
    }

    private int getInt(JsonNode node, String field, int defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asInt() : defaultValue;
    }

    // 内部包装类，用于序列化
    private record SearchResultWrapper(List<DocSearchResult> documents, String query, int total) {
        SearchResultWrapper(List<DocSearchResult> documents, String query) {
            this(documents, query, documents.size());
        }
    }
}
