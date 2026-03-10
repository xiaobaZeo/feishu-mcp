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
}