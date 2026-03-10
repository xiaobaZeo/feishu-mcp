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
 * 飞书用户服务
 */
@Service
public class FeishuUserService {

    private static final Logger log = LoggerFactory.getLogger(FeishuUserService.class);

    private final FeishuAuthService feishuAuthService;
    private final FeishuProperties feishuProperties;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    public FeishuUserService(FeishuAuthService feishuAuthService, FeishuProperties feishuProperties, ObjectMapper objectMapper) {
        this.feishuAuthService = feishuAuthService;
        this.feishuProperties = feishuProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 根据关键词搜索企业用户
     */
    public List<Map<String, Object>> searchUsers(String keyword, int pageSize, int page) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/contact/v3/users/find_by_cha";

        String requestBody = String.format("""
                {
                    "keyword": "%s",
                    "page_size": %d,
                    "page": %d
                }
                """, keyword, pageSize, page);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            List<Map<String, Object>> users = new ArrayList<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("items")) {
                JsonNode items = jsonNode.get("data").get("items");
                for (JsonNode item : items) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("user_id", item.has("user_id") ? item.get("user_id").asText() : null);
                    user.put("name", item.has("name") ? item.get("name").asText() : null);
                    user.put("avatar", item.has("avatar") && item.get("avatar").has("avatar_origin")
                            ? item.get("avatar").get("avatar_origin").asText() : null);
                    user.put("union_id", item.has("union_id") ? item.get("union_id").asText() : null);
                    users.add(user);
                }
            }
            return users;
        }
    }

    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserInfo(String userId) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/contact/v3/users/" + userId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            Map<String, Object> userInfo = new HashMap<>();
            if (jsonNode.has("data") && jsonNode.get("data").has("user")) {
                JsonNode user = jsonNode.get("data").get("user");
                userInfo.put("user_id", user.has("user_id") ? user.get("user_id").asText() : null);
                userInfo.put("union_id", user.has("union_id") ? user.get("union_id").asText() : null);
                userInfo.put("name", user.has("name") ? user.get("name").asText() : null);
                userInfo.put("en_name", user.has("en_name") ? user.get("en_name").asText() : null);
                userInfo.put("avatar", user.has("avatar") && user.get("avatar").has("avatar_origin")
                        ? user.get("avatar").get("avatar_origin").asText() : null);
                userInfo.put("email", user.has("email") ? user.get("email").asText() : null);
                userInfo.put("mobile", user.has("mobile") ? user.get("mobile").asText() : null);
                userInfo.put("department_ids", user.has("department_ids") ? user.get("department_ids") : null);
            }
            return userInfo;
        }
    }

    /**
     * 获取当前用户（调用者）的用户信息
     */
    public Map<String, Object> getCurrentUserInfo() throws IOException {
        return getUserInfo("me");
    }
}