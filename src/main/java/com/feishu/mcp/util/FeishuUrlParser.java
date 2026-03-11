package com.feishu.mcp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 飞书 URL 解析工具
 * 支持从各种格式的飞书文档 URL 中提取文档 ID
 */
public class FeishuUrlParser {

    // 支持的 URL 格式正则
    private static final Pattern WIKI_PATTERN = Pattern.compile("https?://[^/]+/(wiki|docx)/([a-zA-Z0-9_-]+)");
    private static final Pattern DOCUMENT_PATTERN = Pattern.compile("https?://open\\.feishu\\.cn/document/([a-zA-Z0-9_-]+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{10,}$");

    private FeishuUrlParser() {
        // 私有构造函数，防止实例化
    }

    /**
     * 从各种格式的飞书 URL 中提取文档 ID
     * 支持的格式：
     * - https://xxx.feishu.cn/wiki/XXXX
     * - https://xxx.feishu.cn/docx/XXXX
     * - https://open.feishu.cn/document/XXXX
     * - 纯文档ID（直接返回）
     *
     * @param input 文档ID或URL
     * @return 文档ID
     * @throws IllegalArgumentException 如果无法解析
     */
    public static String extractDocumentId(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("文档 ID 或 URL 不能为空");
        }

        String trimmed = input.trim();

        // 如果已经是纯 ID 格式（不包含斜杠），直接返回
        if (!trimmed.contains("/") && TOKEN_PATTERN.matcher(trimmed).matches()) {
            return trimmed;
        }

        // 匹配 wiki/docx URL
        Matcher wikiMatcher = WIKI_PATTERN.matcher(trimmed);
        if (wikiMatcher.find()) {
            return wikiMatcher.group(2);
        }

        // 匹配 open.feishu.cn/document URL
        Matcher docMatcher = DOCUMENT_PATTERN.matcher(trimmed);
        if (docMatcher.find()) {
            return docMatcher.group(1);
        }

        throw new IllegalArgumentException("无法从输入解析文档 ID: " + input + "\n" +
                "支持的格式：\n" +
                "- 纯文档ID（如：XkJdwqZqYlxxxxxx）\n" +
                "- https://xxx.feishu.cn/wiki/XXXX\n" +
                "- https://xxx.feishu.cn/docx/XXXX\n" +
                "- https://open.feishu.cn/document/XXXX");
    }

    /**
     * 判断输入是否为有效的文档 ID 或 URL
     */
    public static boolean isValidDocumentIdOrUrl(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String trimmed = input.trim();

        // 纯 ID 格式
        if (!trimmed.contains("/") && TOKEN_PATTERN.matcher(trimmed).matches()) {
            return true;
        }

        // URL 格式
        return WIKI_PATTERN.matcher(trimmed).find() ||
               DOCUMENT_PATTERN.matcher(trimmed).find();
    }

    /**
     * 构建标准文档 URL
     *
     * @param documentId 文档ID
     * @return 标准文档URL
     */
    public static String buildDocumentUrl(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            throw new IllegalArgumentException("文档 ID 不能为空");
        }
        return "https://open.feishu.cn/document/" + documentId;
    }

    /**
     * 从 URL 中提取工作空间标识
     * 例如：从 https://xxx.feishu.cn/wiki/XXXX 提取 xxx
     *
     * @param url 文档URL
     * @return 工作空间标识，如果不包含则返回 null
     */
    public static String extractWorkspace(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("https?://([^.]+)\\.feishu\\.cn");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
