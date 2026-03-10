package com.feishu.mcp.mcp.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON-RPC处理器测试
 */
class JsonRpcHandlerTest {

    private JsonRpcHandler jsonRpcHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // 创建测试用的McpTool
        McpTool testTool = new McpTool() {
            @Override
            public String getName() {
                return "test_tool";
            }

            @Override
            public String getDescription() {
                return "A test tool";
            }

            @Override
            public JsonNode getInputSchema() {
                ObjectNode schema = objectMapper.createObjectNode();
                schema.put("type", "object");
                return schema;
            }

            @Override
            public JsonNode execute(JsonNode parameters) throws Exception {
                ObjectNode result = objectMapper.createObjectNode();
                result.put("success", true);
                return result;
            }
        };

        jsonRpcHandler = new JsonRpcHandler(objectMapper, Arrays.asList(testTool));
        jsonRpcHandler.initialize();
    }

    @Test
    void testHandleInitialize() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("initialize");
        request.setId("1");

        // when
        JsonRpcResponse response = jsonRpcHandler.handle(request);

        // then
        assertNotNull(response);
        assertEquals("1", response.getId());
        assertNotNull(response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testHandleToolsList() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("tools/list");
        request.setId("2");

        // when
        JsonRpcResponse response = jsonRpcHandler.handle(request);

        // then
        assertNotNull(response);
        assertEquals("2", response.getId());
        assertNotNull(response.getResult());
    }

    @Test
    void testHandleMethodNotFound() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("unknown/method");
        request.setId("3");

        // when
        JsonRpcResponse response = jsonRpcHandler.handle(request);

        // then
        assertNotNull(response);
        assertEquals("3", response.getId());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
    }

    @Test
    void testHandleToolsCall() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("tools/call");
        request.setId("4");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "test_tool");
        params.put("arguments", new HashMap<>());
        request.setParams(params);

        // when
        JsonRpcResponse response = jsonRpcHandler.handle(request);

        // then
        assertNotNull(response);
        assertEquals("4", response.getId());
        assertNotNull(response.getResult());
    }

    @Test
    void testHandleToolsCall_ToolNotFound() {
        // given
        JsonRpcRequest request = new JsonRpcRequest();
        request.setMethod("tools/call");
        request.setId("5");
        Map<String, Object> params = new HashMap<>();
        params.put("name", "non_existent_tool");
        params.put("arguments", new HashMap<>());
        request.setParams(params);

        // when
        JsonRpcResponse response = jsonRpcHandler.handle(request);

        // then
        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals(-32602, response.getError().getCode());
    }
}