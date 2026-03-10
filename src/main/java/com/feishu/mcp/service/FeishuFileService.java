package com.feishu.mcp.service;

import com.feishu.mcp.config.FeishuProperties;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 飞书文件服务
 */
@Service
public class FeishuFileService {

    private static final Logger log = LoggerFactory.getLogger(FeishuFileService.class);

    private final FeishuAuthService feishuAuthService;
    private final FeishuProperties feishuProperties;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    public FeishuFileService(FeishuAuthService feishuAuthService, FeishuProperties feishuProperties) {
        this.feishuAuthService = feishuAuthService;
        this.feishuProperties = feishuProperties;
    }

    /**
     * 获取文件内容
     * @param fileToken 文件的file_token
     * @return 文件内容的Base64编码
     */
    public Map<String, Object> getFileContent(String fileToken) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/drive/v1/files/" + fileToken + "/content";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            // 飞书文件API返回的是二进制内容
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("响应体为空");
            }

            byte[] content = body.bytes();
            String base64Content = Base64.getEncoder().encodeToString(content);

            Map<String, Object> result = new HashMap<>();
            result.put("file_token", fileToken);
            result.put("content_type", response.header("Content-Type", "application/octet-stream"));
            result.put("content_size", content.length);
            result.put("content_base64", base64Content);

            return result;
        }
    }

    /**
     * 获取文件元信息
     */
    public Map<String, Object> getFileInfo(String fileToken) throws IOException {
        String url = feishuProperties.getApiBaseUrl() + "/drive/v1/files/" + fileToken;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + feishuAuthService.getAccessToken())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body().string();
            // 简单解析返回
            Map<String, Object> result = new HashMap<>();
            result.put("raw_response", responseBody);
            return result;
        }
    }
}