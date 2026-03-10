package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文件内容工具 - 获取文件内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileContentTool implements McpTool {

    private final FeishuFileService feishuFileService;
    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "file_content";
    }

    @Override
    public String getDescription() {
        return "根据文件ID(file_token)，获取文档中指定资源（文件或图片）的二进制内容。返回Base64编码的内容。";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();
        properties.set("file_token", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文件的file_token"));

        schema.set("properties", properties);
        schema.put("required", objectMapper.createArrayNode().add("file_token"));
        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        String fileToken = parameters.get("file_token").asText();

        Map<String, Object> fileContent = feishuFileService.getFileContent(fileToken);

        return objectMapper.valueToTree(fileContent);
    }
}