package com.feishu.mcp.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC 2.0 响应
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {

    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private JsonRpcError error;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    public static JsonRpcResponse success(String id, Object result) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(id);
        response.setResult(result);
        return response;
    }

    public static JsonRpcResponse error(String id, int code, String message) {
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(id);
        JsonRpcError error = new JsonRpcError();
        error.setCode(code);
        error.setMessage(message);
        response.setError(error);
        return response;
    }

    public static class JsonRpcError {
        private int code;
        private String message;
        private Object data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}