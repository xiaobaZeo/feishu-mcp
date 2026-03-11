package com.feishu.mcp.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.dto.doc.FeishuApiResponse;
import com.feishu.mcp.exception.FeishuApiException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 统一 HTTP 客户端工具
 * 封装 OkHttp 客户端，提供统一的请求处理和错误处理
 */
@Slf4j
@Component
public class HttpClientUtil {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpClientUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 执行 GET 请求
     *
     * @param url   请求 URL
     * @param token 认证 Token
     * @param <T>   响应数据类型
     * @return 飞书 API 响应
     */
    public <T> FeishuApiResponse<T> get(String url, String token, Class<T> dataClass) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, dataClass);
    }

    /**
     * 执行 GET 请求（使用 TypeReference 支持泛型）
     */
    public <T> FeishuApiResponse<T> get(String url, String token, TypeReference<T> typeReference) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, typeReference);
    }

    /**
     * 执行 POST 请求
     *
     * @param url   请求 URL
     * @param token 认证 Token
     * @param body  请求体对象
     * @param <T>   响应数据类型
     * @return 飞书 API 响应
     */
    public <T> FeishuApiResponse<T> post(String url, String token, Object body, Class<T> dataClass) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, dataClass);
    }

    /**
     * 执行 POST 请求（使用 TypeReference）
     */
    public <T> FeishuApiResponse<T> post(String url, String token, Object body, TypeReference<T> typeReference) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, typeReference);
    }

    /**
     * 执行 PATCH 请求
     */
    public <T> FeishuApiResponse<T> patch(String url, String token, Object body, Class<T> dataClass) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, dataClass);
    }

    /**
     * 执行 PATCH 请求（使用 TypeReference）
     */
    public <T> FeishuApiResponse<T> patch(String url, String token, Object body, TypeReference<T> typeReference) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .patch(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, typeReference);
    }

    /**
     * 执行 DELETE 请求（带请求体）
     */
    public <T> FeishuApiResponse<T> delete(String url, String token, Object body, Class<T> dataClass) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .delete(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, dataClass);
    }

    /**
     * 执行 DELETE 请求（使用 TypeReference）
     */
    public <T> FeishuApiResponse<T> delete(String url, String token, Object body, TypeReference<T> typeReference) throws IOException {
        String jsonBody = body instanceof String ? (String) body : objectMapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(url)
                .delete(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        return execute(request, typeReference);
    }

    /**
     * 执行请求并解析响应
     */
    private <T> FeishuApiResponse<T> execute(Request request, Class<T> dataClass) throws IOException {
        log.debug("HTTP 请求: {} {}", request.method(), request.url());

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("HTTP 响应: {} - {}", response.code(), responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            FeishuApiResponse<T> apiResponse = new FeishuApiResponse<>();

            if (jsonNode.has("code")) {
                apiResponse.setCode(jsonNode.get("code").asInt());
            }
            if (jsonNode.has("msg")) {
                apiResponse.setMsg(jsonNode.get("msg").asText());
            }

            if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
                T data = objectMapper.treeToValue(jsonNode.get("data"), dataClass);
                apiResponse.setData(data);
            }

            // 如果响应不成功，抛出异常
            if (!apiResponse.isSuccess()) {
                throw new FeishuApiException(apiResponse.getCode(), apiResponse.getMsg(), responseBody);
            }

            return apiResponse;
        }
    }

    /**
     * 执行请求并解析响应（使用 TypeReference 支持泛型）
     */
    private <T> FeishuApiResponse<T> execute(Request request, TypeReference<T> typeReference) throws IOException {
        log.debug("HTTP 请求: {} {}", request.method(), request.url());

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            log.debug("HTTP 响应: {} - {}", response.code(), responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            FeishuApiResponse<T> apiResponse = new FeishuApiResponse<>();

            if (jsonNode.has("code")) {
                apiResponse.setCode(jsonNode.get("code").asInt());
            }
            if (jsonNode.has("msg")) {
                apiResponse.setMsg(jsonNode.get("msg").asText());
            }

            if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
                T data = objectMapper.readValue(jsonNode.get("data").toString(), typeReference);
                apiResponse.setData(data);
            }

            // 如果响应不成功，抛出异常
            if (!apiResponse.isSuccess()) {
                throw new FeishuApiException(apiResponse.getCode(), apiResponse.getMsg(), responseBody);
            }

            return apiResponse;
        }
    }
}
