package com.feishu.mcp.exception;

/**
 * 飞书 API 调用异常
 */
public class FeishuApiException extends RuntimeException {

    private final int code;
    private final String apiMessage;

    /**
     * 创建飞书 API 异常
     *
     * @param code      飞书 API 错误码
     * @param message   飞书 API 错误消息
     */
    public FeishuApiException(int code, String message) {
        super("飞书API错误 [" + code + "]: " + message);
        this.code = code;
        this.apiMessage = message;
    }

    /**
     * 创建飞书 API 异常（带原始响应）
     *
     * @param code       飞书 API 错误码
     * @param message    飞书 API 错误消息
     * @param rawResponse 原始响应内容
     */
    public FeishuApiException(int code, String message, String rawResponse) {
        super("飞书API错误 [" + code + "]: " + message + " | 原始响应: " + rawResponse);
        this.code = code;
        this.apiMessage = message;
    }

    public int getCode() {
        return code;
    }

    public String getApiMessage() {
        return apiMessage;
    }
}
