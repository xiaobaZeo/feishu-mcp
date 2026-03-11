package com.feishu.mcp.exception;

import com.feishu.mcp.constant.McpConstants;
import com.feishu.mcp.mcp.protocol.JsonRpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<JsonRpcResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                McpConstants.ERROR_INVALID_PARAMS,
                "Invalid params: " + e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(FeishuApiException.class)
    public ResponseEntity<JsonRpcResponse> handleFeishuApiException(FeishuApiException e) {
        log.error("飞书API调用失败: [{}] {}", e.getCode(), e.getApiMessage());
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                McpConstants.ERROR_SERVER_ERROR,
                "Feishu API error [" + e.getCode() + "]: " + e.getApiMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }

    @ExceptionHandler(DocumentOperationException.class)
    public ResponseEntity<JsonRpcResponse> handleDocumentOperationException(DocumentOperationException e) {
        log.error("文档操作失败: {}", e.getMessage());
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                McpConstants.ERROR_INTERNAL_ERROR,
                "Document operation failed: " + e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<JsonRpcResponse> handleIOException(IOException e) {
        log.error("IO错误: {}", e.getMessage(), e);
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                McpConstants.ERROR_INTERNAL_ERROR,
                "IO error: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcResponse> handleGenericException(Exception e) {
        log.error("未预期的错误: {}", e.getMessage(), e);
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                McpConstants.ERROR_INTERNAL_ERROR,
                "Internal error: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}