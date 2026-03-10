package com.feishu.mcp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feishu.mcp.service.FeishuUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户搜索工具测试
 */
@ExtendWith(MockitoExtension.class)
class UserSearchToolTest {

    @Mock
    private FeishuUserService feishuUserService;

    private UserSearchTool userSearchTool;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        userSearchTool = new UserSearchTool(feishuUserService, objectMapper);
    }

    @Test
    void testGetName() {
        assertEquals("user_search", userSearchTool.getName());
    }

    @Test
    void testGetDescription() {
        assertNotNull(userSearchTool.getDescription());
        assertTrue(userSearchTool.getDescription().contains("搜索"));
    }

    @Test
    void testGetInputSchema() {
        JsonNode schema = userSearchTool.getInputSchema();
        assertNotNull(schema);
        assertEquals("object", schema.get("type").asText());
        assertTrue(schema.has("properties"));
        assertTrue(schema.has("required"));
    }

    @Test
    void testExecute() throws Exception {
        // given
        Map<String, Object> user = new HashMap<>();
        user.put("user_id", "test_user_id");
        user.put("name", "测试用户");

        when(feishuUserService.searchUsers(anyString(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList(user));

        ObjectNode params = objectMapper.createObjectNode();
        params.put("keyword", "测试");
        params.put("page_size", 10);
        params.put("page", 1);

        // when
        JsonNode result = userSearchTool.execute(params);

        // then
        assertNotNull(result);
        assertTrue(result.has("users"));
        assertTrue(result.has("total"));
        assertEquals("测试", result.get("keyword").asText());

        verify(feishuUserService).searchUsers("测试", 10, 1);
    }

    @Test
    void testExecute_WithDefaults() throws Exception {
        // given
        when(feishuUserService.searchUsers(anyString(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());

        ObjectNode params = objectMapper.createObjectNode();
        params.put("keyword", "张三");

        // when
        JsonNode result = userSearchTool.execute(params);

        // then
        verify(feishuUserService).searchUsers("张三", 10, 1);
    }
}