package com.feishu.mcp.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.constant.McpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON-RPC请求处理器
 */
@Component
public class JsonRpcHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonRpcHandler.class);

    private final ObjectMapper objectMapper;
    private final List<McpTool> tools;

    private final Map<String, McpTool> toolMap = new HashMap<>();

    public JsonRpcHandler(ObjectMapper objectMapper, List<McpTool> tools) {
        this.objectMapper = objectMapper;
        this.tools = tools;
    }

    public void initialize() {
        for (McpTool tool : tools) {
            toolMap.put(tool.getName(), tool);
            log.info("注册MCP工具: {}", tool.getName());
        }
    }

    /**
     * 处理JSON-RPC请求
     */
    public JsonRpcResponse handle(JsonRpcRequest request) {
        String method = request.getMethod();
        String id = request.getId();

        try {
            log.debug("处理MCP请求: method={}, id={}", method, id);

            return switch (method) {
                case McpConstants.METHOD_INITIALIZE -> handleInitialize(id);
                case McpConstants.METHOD_TOOLS_LIST -> handleToolsList(id);
                case McpConstants.METHOD_TOOLS_CALL -> handleToolsCall(id, request.getParams());
                case McpConstants.METHOD_RESOURCES_LIST -> handleResourcesList(id);
                case McpConstants.METHOD_RESOURCES_TEMPLATES_LIST -> handleResourceTemplatesList(id);
                default ->
                        JsonRpcResponse.error(id, McpConstants.ERROR_METHOD_NOT_FOUND, "Method not found: " + method);
            };
        } catch (Exception e) {
            log.error("处理请求失败: method={}", method, e);
            return JsonRpcResponse.error(id, McpConstants.ERROR_INTERNAL_ERROR, "Internal error: " + e.getMessage());
        }
    }

    /**
     * 处理initialize请求
     */
    private JsonRpcResponse handleInitialize(String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", McpConstants.PROTOCOL_VERSION);
        result.put("serverInfo", Map.of(
                "name", "feishu-mcp-server",
                "version", "1.0.0"
        ));
        result.put("capabilities", Map.of(
                "tools", Map.of(),
                "resources", Map.of()
        ));
        return JsonRpcResponse.success(id, result);
    }

    /**
     * 处理tools/list请求
     */
    private JsonRpcResponse handleToolsList(String id) {
        List<Map<String, Object>> toolsList = tools.stream()
                .map(tool -> {
                    Map<String, Object> toolInfo = new HashMap<>();
                    toolInfo.put("name", tool.getName());
                    toolInfo.put("description", tool.getDescription());
                    toolInfo.put("inputSchema", tool.getInputSchema());
                    return toolInfo;
                })
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("tools", toolsList);
        return JsonRpcResponse.success(id, result);
    }

    /**
     * 处理tools/call请求
     */
    private JsonRpcResponse handleToolsCall(String id, Map<String, Object> params) {
        String toolName = (String) params.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

        McpTool tool = toolMap.get(toolName);
        if (tool == null) {
            return JsonRpcResponse.error(id, McpConstants.ERROR_INVALID_PARAMS, "Tool not found: " + toolName);
        }

        try {
            JsonNode argsNode = objectMapper.valueToTree(arguments);
            JsonNode result = tool.execute(argsNode);

            Map<String, Object> response = new HashMap<>();
            response.put("content", List.of(Map.of(
                    "type", "text",
                    "text", result.toString()
            )));

            return JsonRpcResponse.success(id, response);
        } catch (Exception e) {
            log.error("执行工具失败: tool={}", toolName, e);
            return JsonRpcResponse.error(id, McpConstants.ERROR_INTERNAL_ERROR, "Tool execution failed: " + e.getMessage());
        }
    }

    /**
     * 处理resources/list请求
     */
    private JsonRpcResponse handleResourcesList(String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("resources", List.of());
        return JsonRpcResponse.success(id, result);
    }

    /**
     * 处理resources/templates/list请求
     */
    private JsonRpcResponse handleResourceTemplatesList(String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("resourceTemplates", List.of());
        return JsonRpcResponse.success(id, result);
    }
}