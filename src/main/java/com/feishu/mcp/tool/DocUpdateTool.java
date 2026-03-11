package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.constant.DocBlockType;
import com.feishu.mcp.dto.doc.DocUpdateResponse;
import com.feishu.mcp.dto.doc.DocumentLocation;
import com.feishu.mcp.mcp.protocol.McpTool;
import com.feishu.mcp.service.FeishuDocService;
import com.feishu.mcp.util.FeishuUrlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import java.util.Arrays;

/**
 * 文档更新工具 - 更新云文档
 * 支持多种定位方式：block_index、block_type、block_id、position_hint
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
        return "在云文档指定位置增加或替换内容。支持多种定位方式：block_index（块索引）、block_type（块类型）、block_id（块ID）、position_hint（位置描述如'第一段之后'）。";
    }

    @Override
    public JsonNode getInputSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");

        ObjectNode properties = objectMapper.createObjectNode();

        // 文档ID（支持URL）
        properties.set("document_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "文档ID（可以直接使用ID，或传入飞书文档URL）"));

        // 内容
        properties.set("text", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "要插入或替换的文本内容"));

        // 操作类型
        ArrayNode operationEnum = objectMapper.createArrayNode();
        operationEnum.add("replace");
        operationEnum.add("insert");
        operationEnum.add("append");
        properties.set("operation", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "操作类型：replace（替换）、insert（追加到现有内容后）、append（在指定位置后新增）")
                .put("default", "replace")
                .set("enum", operationEnum));

        // 定位参数（多种方式，至少提供一种）
        properties.set("block_index", objectMapper.createObjectNode()
                .put("type", "integer")
                .put("description", "【定位方式1】内容块索引（从0开始），0表示第一个可编辑内容块"));

        ArrayNode blockTypeEnum = objectMapper.createArrayNode();
        Arrays.stream(DocBlockType.values())
                .filter(t -> t != DocBlockType.PAGE && t.isTextEditable())
                .forEach(t -> blockTypeEnum.add(t.name().toLowerCase()));
        properties.set("block_type", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "【定位方式2】块类型，如 text、heading1、heading2、bulleted_list 等")
                .set("enum", blockTypeEnum));

        properties.set("block_id", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "【定位方式3】直接指定块ID"));

        properties.set("position_hint", objectMapper.createObjectNode()
                .put("type", "string")
                .put("description", "【定位方式4】位置描述，如'开头'、'末尾'、'第一段之后'、'第一个标题'等"));

        schema.set("properties", properties);

        ArrayNode required = objectMapper.createArrayNode();
        required.add("document_id");
        required.add("text");
        schema.set("required", required);

        return schema;
    }

    @Override
    public JsonNode execute(JsonNode parameters) throws Exception {
        // 解析文档ID（支持URL）
        String documentId = FeishuUrlParser.extractDocumentId(parameters.get("document_id").asText());
        String text = parameters.get("text").asText();
        String operation = getString(parameters, "operation", "replace");

        // 构建定位信息
        DocumentLocation location = DocumentLocation.builder()
                .blockId(getString(parameters, "block_id", null))
                .blockIndex(getInteger(parameters, "block_index", null))
                .blockType(getBlockType(parameters))
                .positionHint(getString(parameters, "position_hint", null))
                .build();

        // 验证至少有一种定位方式
        if (!location.isValid()) {
            throw new IllegalArgumentException("必须指定至少一种定位方式：block_index、block_type、block_id 或 position_hint 其中之一");
        }

        log.info("更新文档: document_id={}, operation={}, location={}",
                documentId, operation, location.getLocationDescription());

        DocUpdateResponse result = feishuDocService.updateDocContent(documentId, text, operation, location);

        return objectMapper.valueToTree(result);
    }

    private DocBlockType getBlockType(JsonNode parameters) {
        if (parameters.has("block_type") && !parameters.get("block_type").isNull()) {
            return DocBlockType.fromString(parameters.get("block_type").asText());
        }
        return null;
    }

    private String getString(JsonNode node, String field, String defaultValue) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : defaultValue;
    }

    private Integer getInteger(JsonNode node, String field, Integer defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).intValue();
        }
        return defaultValue;
    }
}
