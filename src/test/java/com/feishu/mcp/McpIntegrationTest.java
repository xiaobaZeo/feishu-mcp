package com.feishu.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.mcp.protocol.JsonRpcRequest;
import com.feishu.mcp.mcp.protocol.JsonRpcResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MCP 集成测试
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testToolsEndpoint() {
        // when
        ResponseEntity<JsonRpcResponse> response = restTemplate.getForEntity(
                "/mcp/tools",
                JsonRpcResponse.class
        );

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getResult());
    }

    @Test
    void testInitialize() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("initialize");
        request.setId("1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<JsonRpcRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<JsonRpcResponse> response = restTemplate.exchange(
                "/mcp",
                HttpMethod.POST,
                entity,
                JsonRpcResponse.class
        );

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("1", response.getBody().getId());
        assertNotNull(response.getBody().getResult());
    }

    @Test
    void testToolsList() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setMethod("tools/list");
        request.setId("2");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<JsonRpcRequest> entity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<JsonRpcResponse> response = restTemplate.exchange(
                "/mcp",
                HttpMethod.POST,
                entity,
                JsonRpcResponse.class
        );

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("2", response.getBody().getId());
        assertNotNull(response.getBody().getResult());
    }
}