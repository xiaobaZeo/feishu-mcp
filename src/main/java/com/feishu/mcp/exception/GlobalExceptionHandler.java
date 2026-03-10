package com.feishu.mcp.exception;

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
                -32602,
                "Invalid params: " + e.getMessage()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<JsonRpcResponse> handleIOException(IOException e) {
        log.error("IO错误: {}", e.getMessage(), e);
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                -32603,
                "IO error: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonRpcResponse> handleGenericException(Exception e) {
        log.error("未预期的错误: {}", e.getMessage(), e);
        JsonRpcResponse response = JsonRpcResponse.error(
                null,
                -32603,
                "Internal error: " + e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}