package com.feishu.mcp.constant;

/**
 * MCP 协议常量
 */
public class McpConstants {

    // JSON-RPC 错误码
    public static final int ERROR_PARSE_ERROR = -32700;
    public static final int ERROR_INVALID_REQUEST = -32600;
    public static final int ERROR_METHOD_NOT_FOUND = -32601;
    public static final int ERROR_INVALID_PARAMS = -32602;
    public static final int ERROR_INTERNAL_ERROR = -32603;
    public static final int ERROR_SERVER_ERROR = -32000;

    // MCP 方法名
    public static final String METHOD_INITIALIZE = "initialize";
    public static final String METHOD_TOOLS_LIST = "tools/list";
    public static final String METHOD_TOOLS_CALL = "tools/call";
    public static final String METHOD_RESOURCES_LIST = "resources/list";
    public static final String METHOD_RESOURCES_TEMPLATES_LIST = "resources/templates/list";

    // MCP 协议版本
    public static final String PROTOCOL_VERSION = "2024-11-05";

    // 传输模式
    public static final String TRANSPORT_STDIO = "stdio";
    public static final String TRANSPORT_HTTP = "http";
    public static final String TRANSPORT_BOTH = "both";

    // 认证模式
    public static final String AUTH_MODE_APP = "app";
    public static final String AUTH_MODE_USER = "user";

    // 飞书 API 相关
    public static final String FEISHU_API_BASE_URL = "https://open.feishu.cn/open_api";
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE = 1;

    // 文档操作相关
    public static final String DOC_OPERATION_REPLACE = "replace";
    public static final String DOC_OPERATION_INSERT = "insert";
    public static final String DOC_OPERATION_APPEND = "append";

    // 内容格式
    public static final String FORMAT_PLAIN = "plain";
    public static final String FORMAT_MARKDOWN = "markdown";

    private McpConstants() {
        // 私有构造函数，防止实例化
    }
}