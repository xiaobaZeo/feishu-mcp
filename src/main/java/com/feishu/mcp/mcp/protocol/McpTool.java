package com.feishu.mcp.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * MCP工具接口
 */
public interface McpTool {

    /**
     * 获取工具名称
     */
    String getName();

    /**
     * 获取工具描述
     */
    String getDescription();

    /**
     * 获取输入参数schema
     */
    JsonNode getInputSchema();

    /**
     * 执行工具
     */
    JsonNode execute(JsonNode parameters) throws Exception;
}