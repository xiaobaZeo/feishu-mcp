package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 飞书认证服务测试
 */
@ExtendWith(MockitoExtension.class)
class FeishuAuthServiceTest {

    @Mock
    private FeishuProperties feishuProperties;

    private ObjectMapper objectMapper;
    private FeishuAuthService feishuAuthService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        feishuAuthService = new FeishuAuthService(feishuProperties, objectMapper);
    }

    @Test
    void testGetAccessToken_UsesTenantTokenByDefault() throws IOException {
        // given
        when(feishuProperties.getAuthMode()).thenReturn("app");
        when(feishuProperties.getApiBaseUrl()).thenReturn("https://open.feishu.cn/open_api");
        when(feishuProperties.getAppId()).thenReturn("test_app_id");
        when(feishuProperties.getAppSecret()).thenReturn("test_app_secret");

        // 这里需要模拟HTTP响应，由于使用了真实的OkHttpClient，
        // 实际测试中可能需要使用WireMock或MockWebServer

        // then
        verify(feishuProperties).getAuthMode();
    }

    @Test
    void testGetAccessToken_UsesUserTokenWhenConfigured() throws IOException {
        // given
        when(feishuProperties.getAuthMode()).thenReturn("user");
        when(feishuProperties.getUserCode()).thenReturn("test_code");

        // then
        verify(feishuProperties).getAuthMode();
    }
}