package com.feishu.mcp.controller;

import com.feishu.mcp.mcp.protocol.JsonRpcHandler;
import com.feishu.mcp.mcp.protocol.JsonRpcRequest;
import com.feishu.mcp.mcp.protocol.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.UUID;

/**
 * MCP HTTP控制器 - 支持同步HTTP请求/响应模式
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final JsonRpcHandler jsonRpcHandler;

    public McpController(JsonRpcHandler jsonRpcHandler) {
        this.jsonRpcHandler = jsonRpcHandler;
    }

    /**
     * 处理JSON-RPC请求
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<JsonRpcResponse> handleMessage(@RequestBody JsonRpcRequest request) {
        log.debug("收到消息: method={}", request.getMethod());
        try {
            JsonRpcResponse response = jsonRpcHandler.handle(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理消息失败", e);
            return ResponseEntity.ok(JsonRpcResponse.error(
                    request.getId(),
                    -32603,
                    "Internal error: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取工具列表（调试用）
     */
    @GetMapping("/tools")
    public ResponseEntity<JsonRpcResponse> listTools() {
        JsonRpcRequest request = createRequest("tools/list", new HashMap<>());
        return ResponseEntity.ok(jsonRpcHandler.handle(request));
    }

    private JsonRpcRequest createRequest(String method, java.util.Map<String, Object> params) {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod(method);
        request.setParams(params);
        request.setId(UUID.randomUUID().toString());
        return request;
    }
}
