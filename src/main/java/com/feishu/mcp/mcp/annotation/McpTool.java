package com.feishu.mcp.mcp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MCP工具注解，用于标记一个类为MCP工具
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {

    /**
     * 工具名称
     */
    String name();

    /**
     * 工具描述
     */
    String description();
}