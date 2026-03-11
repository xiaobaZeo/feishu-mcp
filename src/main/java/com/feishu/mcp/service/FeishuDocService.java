package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 飞书云文档服务
 */
@Service
public class FeishuDocService {

    private static final Logger log = LoggerFactory.getLogger(FeishuDocService.class);

    private final FeishuAuthService feishuAuthService;
    private final FeishuProperties feishuProperties;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public FeishuDocService(FeishuAuthService feishuAuthService, FeishuProperties feishuProperties, ObjectMapper objectMapper) {
        this.feishuAuthService = feishuAuthService;
        this.feishuProperties = feishuProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 搜索云文档
     * @param query 关键词
     * @param creator 创建者ID
     * @param pageSize 每页数量
     * @param page 页码
     */
    public List<Map<String, Object>> searchDocs(String query, String creator, int pageSize, int page) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/search/v1/quick";

        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("{");
        requestBodyBuilder.append("\"query\": \"").append(query).append("\"");
        requestBodyBuilder.append(", \"page_size\": ").append(pageSize);
        requestBodyBuilder.append(", \"page\": ").append(page);
        if (creator != null && !creator.isEmpty()) {
            requestBodyBuilder.append(", \"creator\": \"").append(creator).append("\"");
        }
        requestBodyBuilder.append("}");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBodyBuilder.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<Map<String, Object>> docs = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("document_id", item.has("document_id") ? item.get("document_id").asText() : null);
                    doc.put("title", item.has("title") ? item.get("title").asText() : null);
                    doc.put("doc_type", item.has("doc_type") ? item.get("doc_type").asText() : null);
                    doc.put("url", item.has("url") ? item.get("url").asText() : null);
                    doc.put("creator", item.has("creator") ? item.get("creator").asText() : null);
                    doc.put("created_time", item.has("created_time") ? item.get("created_time").asText() : null);
                    doc.put("updated_time", item.has("updated_time") ? item.get("updated_time").asText() : null);
                    docs.add(doc);
                }
            }
            return docs;
        }
    }

    /**
     * 创建云文档
     * @param nodeId 知识空间节点ID（可选，为空则创建在我的文档库）
     * @param title 文档标题
     * @param content 文档内容（可选）
     */
    public Map<String, Object> createDoc(String nodeId, String title, String content) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents";

        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("{");
        requestBodyBuilder.append("\"node_id\": \"").append(nodeId != null ? nodeId : "").append("\"");
        requestBodyBuilder.append(", \"document\": {");
        requestBodyBuilder.append("\"title\": \"").append(title).append("\"");
        if (content != null && !content.isEmpty()) {
            requestBodyBuilder.append(", \"content\": [[{\"tag\": \"text\", \"text\": \"");
            // 转义content中的特殊字符
            String escapedContent = content.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            requestBodyBuilder.append(escapedContent).append("\"}]]");
        }
        requestBodyBuilder.append("}");
        requestBodyBuilder.append("}");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBodyBuilder.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("document")) {
                JsonNode doc = jsonNode.get("data").get("document");
                result.put("document_id", doc.has("document_id") ? doc.get("document_id").asText() : null);
                result.put("token", doc.has("token") ? doc.get("token").asText() : null);
                result.put("url", "https://open.feishu.com/document/" + (doc.has("token") ? doc.get("token").asText() : ""));
            }
            return result;
        }
    }

    /**
     * 获取云文档内容
     */
    public Map<String, Object> getDoc(String documentId) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("document")) {
                JsonNode doc = jsonNode.get("data").get("document");
                result.put("document_id", doc.has("document_id") ? doc.get("document_id").asText() : null);
                result.put("title", doc.has("title") ? doc.get("title").asText() : null);
                result.put("content", doc.has("body") ? doc.get("body").toString() : null);
                result.put("raw_response", jsonNode.toString());
            }
            return result;
        }
    }

    /**
     * 更新云文档
     * @param documentId 文档ID
     * @param requests 更新请求数组
     */
    public Map<String, Object> updateDoc(String documentId, List<Map<String, Object>> requests) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId;

        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("{\"requests\": [");
        for (int i = 0; i < requests.size(); i++) {
            Map<String, Object> req = requests.get(i);
            if (i > 0) requestBodyBuilder.append(", ");
            requestBodyBuilder.append("{");
            requestBodyBuilder.append("\"type\": \"").append(req.get("type")).append("\"");
            requestBodyBuilder.append(", \"range\": {");
            requestBodyBuilder.append("\"start_index\": ").append(req.get("start_index"));
            requestBodyBuilder.append(", \"end_index\": ").append(req.get("end_index"));
            requestBodyBuilder.append("}");
            requestBodyBuilder.append(", \"body\": [");
            requestBodyBuilder.append("[{\"tag\": \"text\", \"text\": \"");
            String text = ((String) req.get("text")).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            requestBodyBuilder.append(text).append("\"}]");
            requestBodyBuilder.append("]");
            requestBodyBuilder.append("}");
        }
        requestBodyBuilder.append("]}");

        Request request = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(requestBodyBuilder.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();
            result.put("success", jsonNode.has("data"));
            result.put("raw_response", jsonNode.toString());
            return result;
        }
    }

    /**
     * 更新云文档内容 - 使用 batch_update 在指定块位置增加或替换内容
     * @param documentId 文档ID
     * @param text 要更新的文本内容
     * @param operation 操作类型：insert（追加）或 replace（替换）
     * @param blockIndex 内容块索引（从0开始，0表示第一个可编辑内容块）
     */
    public Map<String, Object> updateDocContent(String documentId, String text, String operation, int blockIndex) throws IOException {
        // 1. 获取文档块列表
        String blocksUrl = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/blocks";

        Request getRequest = new Request.Builder()
                .url(blocksUrl + "?page_size=500")
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        List<String> editableBlockIds = new ArrayList<>();
        try (Response response = httpClient.newCall(getRequest).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    if (item.has("block_id") && item.has("block_type")) {
                        int blockType = item.get("block_type").asInt();
                        // 跳过 page 块(1)，收集所有可编辑的块
                        if (blockType != 1) {
                            editableBlockIds.add(item.get("block_id").asText());
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();

        if (editableBlockIds.isEmpty()) {
            result.put("success", false);
            result.put("error", "未找到可编辑的块");
            return result;
        }

        if (blockIndex < 0 || blockIndex >= editableBlockIds.size()) {
            result.put("success", false);
            result.put("error", "block_index 超出范围，有效范围是 0 到 " + (editableBlockIds.size() - 1));
            result.put("total_editable_blocks", editableBlockIds.size());
            return result;
        }

        String targetBlockId = editableBlockIds.get(blockIndex);

        // 2. 根据操作类型构建请求体
        String requestBodyJson;
        String escapedText = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");

        if ("insert".equals(operation)) {
            // insert 操作：先获取现有内容，然后追加新内容
            String existingContent = getBlockContent(documentId, targetBlockId);
            String newContent = existingContent + text;
            String escapedNewContent = newContent.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            requestBodyJson = "{\"requests\": [{\"block_id\": \"" + targetBlockId + "\", \"update_text_elements\": {\"elements\": [{\"text_run\": {\"content\": \"" + escapedNewContent + "\"}}]}}]}";
        } else {
            // replace 操作：直接替换为指定内容
            requestBodyJson = "{\"requests\": [{\"block_id\": \"" + targetBlockId + "\", \"update_text_elements\": {\"elements\": [{\"text_run\": {\"content\": \"" + escapedText + "\"}}]}}]}";
        }

        String patchUrl = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/blocks/batch_update";

        Request patchRequest = new Request.Builder()
                .url(patchUrl)
                .patch(RequestBody.create(requestBodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(patchRequest).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            result.put("success", jsonNode.has("data"));
            result.put("raw_response", jsonNode.toString());
            result.put("updated_block_id", targetBlockId);
            result.put("block_index", blockIndex);
            result.put("operation", operation);
        }

        return result;
    }

    /**
     * 获取指定块的内容（辅助方法）
     */
    private String getBlockContent(String documentId, String blockId) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/blocks/" + blockId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("data") && jsonNode.get("data").has("block")) {
                JsonNode block = jsonNode.get("data").get("block");
                // 尝试从不同类型的块中提取文本内容
                String content = extractTextFromBlock(block);
                return content != null ? content : "";
            }
        }
        return "";
    }

    /**
     * 从块中提取文本内容（辅助方法）
     */
    private String extractTextFromBlock(JsonNode block) {
        // 尝试从不同块类型中提取文本
        if (block.has("text") && block.get("text").has("elements")) {
            return extractTextFromElements(block.get("text").get("elements"));
        }
        if (block.has("heading1") && block.get("heading1").has("elements")) {
            return extractTextFromElements(block.get("heading1").get("elements"));
        }
        if (block.has("heading2") && block.get("heading2").has("elements")) {
            return extractTextFromElements(block.get("heading2").get("elements"));
        }
        if (block.has("heading3") && block.get("heading3").has("elements")) {
            return extractTextFromElements(block.get("heading3").get("elements"));
        }
        if (block.has("quote") && block.get("quote").has("elements")) {
            return extractTextFromElements(block.get("quote").get("elements"));
        }
        if (block.has("bulleted_list") && block.get("bulleted_list").has("elements")) {
            return extractTextFromElements(block.get("bulleted_list").get("elements"));
        }
        if (block.has("numbered_list") && block.get("numbered_list").has("elements")) {
            return extractTextFromElements(block.get("numbered_list").get("elements"));
        }
        return "";
    }

    /**
     * 从 elements 中提取文本（辅助方法）
     */
    private String extractTextFromElements(JsonNode elements) {
        StringBuilder text = new StringBuilder();
        for (JsonNode element : elements) {
            if (element.has("text_run") && element.get("text_run").has("content")) {
                text.append(element.get("text_run").get("content").asText());
            }
        }
        return text.toString();
    }

    /**
     * 获取文档评论列表
     */
    public List<Map<String, Object>> getDocComments(String documentId) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/comments";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<Map<String, Object>> comments = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> comment = new HashMap<>();
                    comment.put("comment_id", item.has("comment_id") ? item.get("comment_id").asText() : null);
                    comment.put("content", item.has("content") ? item.get("content").asText() : null);
                    comment.put("created_by", item.has("created_by") ? item.get("created_by").asText() : null);
                    comment.put("created_time", item.has("created_time") ? item.get("created_time").asText() : null);
                    comment.put("is_root", item.has("is_root") ? item.get("is_root").asBoolean() : false);
                    // 划词评论相关
                    if (item.has("quote") && !item.get("quote").isNull()) {
                        comment.put("quote", item.get("quote").asText());
                    }
                    comments.add(comment);
                }
            }
            return comments;
        }
    }

    /**
     * 添加文档评论
     */
    public Map<String, Object> addDocComment(String documentId, String content, String quote) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/comments";

        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("{");
        requestBodyBuilder.append("\"comment\": {");
        requestBodyBuilder.append("\"content\": \"");
        String escapedContent = content.replace("\\", "\\\\").replace("\"", "\\\"");
        requestBodyBuilder.append(escapedContent).append("\"");
        if (quote != null && !quote.isEmpty()) {
            requestBodyBuilder.append(", \"quote\": \"").append(quote).append("\"");
        }
        requestBodyBuilder.append("}");
        requestBodyBuilder.append("}");

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBodyBuilder.toString(), MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("comment")) {
                JsonNode comment = jsonNode.get("data").get("comment");
                result.put("comment_id", comment.has("comment_id") ? comment.get("comment_id").asText() : null);
            }
            result.put("success", jsonNode.has("data"));
            return result;
        }
    }

    /**
     * 清空文档内容 - 删除文档中所有内容块（保留文档本身）
     * 使用 DELETE /documents/{id}/blocks/{block_id}/children/batch_delete 删除 page 块下的所有子块
     * @param documentId 文档ID
     */
    public Map<String, Object> clearDoc(String documentId) throws IOException {
        // 强制刷新token，确保使用最新权限
        feishuAuthService.forceRefreshToken();

        // 1. 获取文档块列表，找到 page 块及其子块
        String blocksUrl = feishuProperties.getApiBaseUrl() + "/docx/v1/documents/" + documentId + "/blocks";

        Request getRequest = new Request.Builder()
                .url(blocksUrl + "?page_size=500")
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        String pageBlockId = null;
        int childrenCount = 0;

        try (Response response = httpClient.newCall(getRequest).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    if (item.has("block_type") && item.has("block_id")) {
                        int blockType = item.get("block_type").asInt();
                        // 找到 page 块
                        if (blockType == 1) {
                            pageBlockId = item.get("block_id").asText();
                            // 获取 page 块的子块数量
                            if (item.has("children") && !item.get("children").isNull()) {
                                childrenCount = item.get("children").size();
                            }
                            break;
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();

        if (pageBlockId == null) {
            result.put("success", false);
            result.put("error", "未找到文档的 page 块");
            return result;
        }

        result.put("page_block_id", pageBlockId);
        result.put("children_count", childrenCount);

        if (childrenCount == 0) {
            result.put("deleted_count", 0);
            result.put("success", true);
            return result;
        }

        // 2. 调用 batch_delete 删除 page 块下的所有子块
        String deleteUrl = feishuProperties.getApiBaseUrl() +
                "/docx/v1/documents/" + documentId +
                "/blocks/" + pageBlockId +
                "/children/batch_delete?document_revision_id=-1";

        String requestBodyJson = "{\"start_index\": 0, \"end_index\": " + childrenCount + "}";

        RequestBody requestBody = RequestBody.create(requestBodyJson, MediaType.parse("application/json; charset=utf-8"));
        Request deleteRequest = new Request.Builder()
                .url(deleteUrl)
                .delete(requestBody)
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();

        try (Response response = httpClient.newCall(deleteRequest).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (response.isSuccessful() && jsonNode.has("code") && jsonNode.get("code").asInt() == 0) {
                result.put("deleted_count", childrenCount);
                result.put("success", true);
                if (jsonNode.has("data") && jsonNode.get("data").has("document_revision_id")) {
                    result.put("document_revision_id", jsonNode.get("data").get("document_revision_id").asInt());
                }
            } else {
                result.put("deleted_count", 0);
                result.put("success", false);
                result.put("error", jsonNode.has("msg") ? jsonNode.get("msg").asText() : "HTTP " + response.code());
                result.put("raw_response", responseBody);
                log.error("批量删除块失败: documentId={}, response={}", documentId, responseBody);
            }
        } catch (Exception e) {
            log.error("批量删除块请求异常: documentId={}, error={}", documentId, e.getMessage());
            result.put("deleted_count", 0);
            result.put("success", false);
            result.put("error", "Exception: " + e.getMessage());
        }

        return result;
    }
}