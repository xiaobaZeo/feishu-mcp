package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 飞书认证服务
 */
@Service
public class FeishuAuthService {

    private static final Logger log = LoggerFactory.getLogger(FeishuAuthService.class);

    private final FeishuProperties feishuProperties;
    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private String tenantAccessToken;
    private long tokenExpireTime = 0;

    public FeishuAuthService(FeishuProperties feishuProperties, ObjectMapper objectMapper) {
        this.feishuProperties = feishuProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取tenant_access_token（应用凭证）
     */
    public String getTenantAccessToken() throws IOException {
        // 检查缓存的token是否有效
        if (tenantAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return tenantAccessToken;
        }

        String url = feishuProperties.getApiBaseUrl() + "/authen/v1/tenant_access_token/internal";

        String requestBody = String.format("""
                {
                    "app_id": "%s",
                    "app_secret": "%s"
                }
                """, feishuProperties.getAppId(), feishuProperties.getAppSecret());

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("tenant_access_token")) {
                tenantAccessToken = jsonNode.get("tenant_access_token").asText();
                // 提前5分钟过期
                long expire = jsonNode.get("expire").asLong() * 1000;
                tokenExpireTime = expire - 5 * 60 * 1000;
                log.debug("获取tenant_access_token成功");
                return tenantAccessToken;
            } else {
                throw new IOException("获取tenant_access_token失败: " + responseBody);
            }
        }
    }

    /**
     * 获取user_access_token（用户授权）
     */
    public String getUserAccessToken(String code) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/authen/v1/authorize_access_token/internal";

        String requestBody = String.format("""
                {
                    "grant_type": "authorization_code",
                    "code": "%s"
                }
                """, code);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("access_token")) {
                return jsonNode.get("access_token").asText();
            } else {
                throw new IOException("获取user_access_token失败: " + responseBody);
            }
        }
    }

    /**
     * 获取当前使用的token
     */
    public String getAccessToken() throws IOException {
        if ("user".equals(feishuProperties.getAuthMode()) && feishuProperties.getUserCode() != null) {
            return getUserAccessToken(feishuProperties.getUserCode());
        }
        return getTenantAccessToken();
    }
}