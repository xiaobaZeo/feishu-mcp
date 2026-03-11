package com.feishu.mcp.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.McpServerProperties;
import com.feishu.mcp.mcp.protocol.JsonRpcHandler;
import com.feishu.mcp.mcp.protocol.JsonRpcRequest;
import com.feishu.mcp.mcp.protocol.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * MCP服务器核心 - 支持STDIO和SSE两种模式
 */
@Component
public class McpServer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(McpServer.class);

    private final JsonRpcHandler jsonRpcHandler;
    private final McpServerProperties mcpServerProperties;
    private final ObjectMapper objectMapper;

    public McpServer(JsonRpcHandler jsonRpcHandler, McpServerProperties mcpServerProperties, ObjectMapper objectMapper) {
        this.jsonRpcHandler = jsonRpcHandler;
        this.mcpServerProperties = mcpServerProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        // 初始化工具注册
        jsonRpcHandler.initialize();

        String transport = mcpServerProperties.getTransport();
        log.info("启动MCP服务器，传输模式: {}", transport);

        if ("stdio".equals(transport) || "both".equals(transport)) {
            startStdioServer();
        }

        // SSE模式由Spring WebFlux控制器处理，不需要在这里启动
    }

    /**
     * 启动STDIO服务器
     */
    private void startStdioServer() {
        log.info("STDIO服务器启动中...");
        Scanner scanner = new Scanner(System.in);

        // 发送初始化消息
        log.debug("等待JSON-RPC请求...");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                continue;
            }

            try {
                JsonRpcRequest request = objectMapper.readValue(line, JsonRpcRequest.class);
                JsonRpcResponse response = jsonRpcHandler.handle(request);
                String responseJson = objectMapper.writeValueAsString(response);
                log.debug("响应: {}", responseJson);
            } catch (Exception e) {
                log.error("处理请求失败", e);
            }
        }
    }
}