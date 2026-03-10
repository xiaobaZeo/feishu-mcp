package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 飞书知识空间服务
 */
@Service
public class FeishuKnowledgeService {

    private static final Logger log = LoggerFactory.getLogger(FeishuKnowledgeService.class);

    private final FeishuAuthService feishuAuthService;
    private final FeishuProperties feishuProperties;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public FeishuKnowledgeService(FeishuAuthService feishuAuthService, FeishuProperties feishuProperties, ObjectMapper objectMapper) {
        this.feishuAuthService = feishuAuthService;
        this.feishuProperties = feishuProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取知识空间节点下的文档列表
     * @param nodeId 知识空间节点ID
     * @param pageSize 每页数量
     * @param pageToken 分页token
     */
    public Map<String, Object> getNodeDocuments(String nodeId, int pageSize, String pageToken) throws IOException {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(feishuProperties.getApiBaseUrl())
                .append("/open_apis/knowledge/v1/nodes/")
                .append(nodeId)
                .append("/documents")
                .append("?page_size=").append(pageSize);

        if (pageToken != null && !pageToken.isEmpty()) {
            urlBuilder.append("&page_token=").append(pageToken);
        }

        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> result = new HashMap<>();

            List<Map<String, Object>> docs = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> doc = new HashMap<>();
                    doc.put("document_id", item.has("document_id") ? item.get("document_id").asText() : null);
                    doc.put("title", item.has("title") ? item.get("title").asText() : null);
                    doc.put("obj_type", item.has("obj_type") ? item.get("obj_type").asText() : null);
                    doc.put("creator", item.has("creator") ? item.get("creator").asText() : null);
                    doc.put("created_time", item.has("created_time") ? item.get("created_time").asText() : null);
                    doc.put("updated_time", item.has("updated_time") ? item.get("updated_time").asText() : null);
                    docs.add(doc);
                }
            }

            result.put("documents", docs);

            if (jsonNode.has("data") && jsonNode.get("data").has("page_token")) {
                result.put("next_page_token", jsonNode.get("data").get("page_token").asText());
            } else {
                result.put("next_page_token", "");
            }

            return result;
        }
    }

    /**
     * 获取知识空间列表
     */
    public List<Map<String, Object>> listSpaces() throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/open_apis/knowledge/v1/spaces";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<Map<String, Object>> spaces = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> space = new HashMap<>();
                    space.put("space_id", item.has("space_id") ? item.get("space_id").asText() : null);
                    space.put("name", item.has("name") ? item.get("name").asText() : null);
                    space.put("space_type", item.has("space_type") ? item.get("space_type").asText() : null);
                    spaces.add(space);
                }
            }
            return spaces;
        }
    }

    /**
     * 获取知识空间节点列表
     */
    public List<Map<String, Object>> listNodes(String spaceId) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/open_apis/knowledge/v1/spaces/" + spaceId + "/nodes";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<Map<String, Object>> nodes = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("node_id", item.has("node_id") ? item.get("node_id").asText() : null);
                    node.put("name", item.has("name") ? item.get("name").asText() : null);
                    node.put("obj_type", item.has("obj_type") ? item.get("obj_type").asText() : null);
                    nodes.add(node);
                }
            }
            return nodes;
        }
    }
}