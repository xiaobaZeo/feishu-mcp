package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuUserService;
import com.feishu.mcp.constant.McpConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 用户搜索工具 - 根据关键词搜索企业内的用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserSearchTool implements McpTool {

    private final FeishuUserService feishuUserService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "user_search";
    }

    @Override
    public String getDescription() {
        return "根据关键词搜索企业内的用户，可获取用户的ID、姓名、头像等信息";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("keyword", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "搜索关键词，可以是姓名、邮箱或部门"));
        properties.set("page_size", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "每页返回数量，默认" + McpConstants.DEFAULT_PAGE_SIZE)
                .put("default", McpConstants.DEFAULT_PAGE_SIZE));
        properties.set("page", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "页码，从1开始")
                .put("default", 1));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("keyword"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String keyword = parameters.has("keyword") ? parameters.get("keyword").asText() : "";
        int pageSize = parameters.has("page_size") ? parameters.get("page_size").asInt() : McpConstants.DEFAULT_PAGE_SIZE;
        int page = parameters.has("page") ? parameters.get("page").asInt() : McpConstants.DEFAULT_PAGE;

        List<Map<String, Object>> users = feishuUserService.searchUsers(keyword, pageSize, page);

        ObjectNode result = objectMapper.createObjectNode();
        result.putPOJO("users", users);
        result.put("total", users.size());
        result.put("keyword", keyword);

        return result;
    }
}