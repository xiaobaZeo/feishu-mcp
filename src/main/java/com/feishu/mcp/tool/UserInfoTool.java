package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户信息工具 - 获取用户个人信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInfoTool implements McpTool {

    private final FeishuUserService feishuUserService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "user_info";
    }

    @Override
    public String getDescription() {
        return "获取用户本人的个人信息，也可通过用户ID获取其他用户的姓名、头像等基本信息";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("user_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "用户ID。如果为空或传'me'，则获取当前用户信息"));

        schema.set("properties", properties);
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String userId = parameters.has("user_id") ? parameters.get("user_id").asText() : "me";

        Map<String, Object> userInfo;
        if ("me".equals(userId) || userId.isEmpty()) {
            userInfo = feishuUserService.getCurrentUserInfo();
        } else {
            userInfo = feishuUserService.getUserInfo(userId);
        }

        return objectMapper.valueToTree(userInfo);
    }
}