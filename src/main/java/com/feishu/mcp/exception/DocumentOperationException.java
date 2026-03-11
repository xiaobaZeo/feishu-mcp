package com.feishu.mcp.exception;

/**
 * 文档操作异常
 */
public class DocumentOperationException extends RuntimeException {

    public DocumentOperationException(String message) {
        super(message);
    }

    public DocumentOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 当块索引超出范围时
     */
    public static DocumentOperationException blockIndexOutOfRange(int index, int maxIndex) {
        return new DocumentOperationException(
            "block_index 超出范围: " + index + ", 有效范围: 0-" + maxIndex);
    }

    /**
     * 当未找到指定类型的块时
     */
    public static DocumentOperationException blockTypeNotFound(String blockType) {
        return new DocumentOperationException("未找到类型为 " + blockType + " 的块");
    }

    /**
     * 当未找到文档的 page 块时
     */
    public static DocumentOperationException pageBlockNotFound() {
        return new DocumentOperationException("未找到文档的 page 块");
    }

    /**
     * 当未指定有效的定位方式时
     */
    public static DocumentOperationException invalidLocation() {
        return new DocumentOperationException("未指定有效的定位方式，" +
            "请提供 block_id、block_index、block_type 或 position_hint 其中之一");
    }
}
