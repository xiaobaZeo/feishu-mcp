package com.feishu.mcp.controller;

import com.feishu.mcp.mcp.protocol.JsonRpcHandler;
import com.feishu.mcp.mcp.protocol.JsonRpcRequest;
import com.feishu.mcp.mcp.protocol.JsonRpcResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP SSE HTTP控制器
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    private static final Logger log = LoggerFactory.getLogger(McpController.class);

    private final JsonRpcHandler jsonRpcHandler;
    private final ObjectMapper objectMapper;

    // 存储SSE会话
    private final Map<String, SseSession> sessions = new ConcurrentHashMap<>();

    public McpController(JsonRpcHandler jsonRpcHandler, ObjectMapper objectMapper) {
        this.jsonRpcHandler = jsonRpcHandler;
        this.objectMapper = objectMapper;
    }

    /**
     * 初始化连接
     */
    @GetMapping
    public Flux<ServerSentEvent<String>> sseConnect() {
        String sessionId = UUID.randomUUID().toString();
        log.info("新SSE连接: {}", sessionId);

        sessions.put(sessionId, new SseSession(sessionId));

        // 发送初始化事件
        Map<String, Object> initParams = new HashMap<>();
        initParams.put("protocolVersion", "2024-11-05");
        initParams.put("capabilities", new HashMap<>());

        JsonRpcResponse initResponse = jsonRpcHandler.handle(createRequest("initialize", initParams));

        String initData = toJson(initResponse);

        return Flux.concat(
                Mono.just(ServerSentEvent.builder(initData).event("initialize").build()),
                Flux.never()
        ).doOnCancel(() -> {
            log.info("SSE连接关闭: {}", sessionId);
            sessions.remove(sessionId);
        });
    }

    /**
     * 发送消息
     */
    @PostMapping
    public Mono<JsonRpcResponse> handleMessage(@RequestBody JsonRpcRequest request) {
        log.debug("收到消息: method={}", request.getMethod());
        try {
            JsonRpcResponse response = jsonRpcHandler.handle(request);
            return Mono.just(response);
        } catch (Exception e) {
            log.error("处理消息失败", e);
            return Mono.just(JsonRpcResponse.error(
                    request.getId(),
                    -32603,
                    "Internal error: " + e.getMessage()
            ));
        }
    }

    /**
     * 工具列表
     */
    @GetMapping("/tools")
    public Mono<JsonRpcResponse> listTools() {
        return Mono.just(jsonRpcHandler.handle(createRequest("tools/list", new HashMap<>())));
    }

    private JsonRpcRequest createRequest(String method, Map<String, Object> params) {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod(method);
        request.setParams(params);
        request.setId(UUID.randomUUID().toString());
        return request;
    }

    private String toJson(JsonRpcResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            log.error("序列化失败", e);
            return "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603}}";
        }
    }

    /**
     * SSE会话
     */
    private static class SseSession {
        final String id;

        SseSession(String id) {
            this.id = id;
        }
    }
}