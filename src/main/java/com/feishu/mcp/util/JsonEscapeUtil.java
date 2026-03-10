package com.feishu.mcp.util;

/**
 * JSON 字符串转义工具
 */
public class JsonEscapeUtil {

    /**
     * 转义字符串中的特殊字符，用于 JSON
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 反转义 JSON 字符串
     */
    public static String unescape(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\b", "\b")
                .replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private JsonEscapeUtil() {
        // 私有构造函数，防止实例化
    }
}