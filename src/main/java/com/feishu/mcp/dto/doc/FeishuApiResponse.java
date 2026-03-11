package com.feishu.mcp.dto.doc;

import lombok.Data;

/**
 * 飞书 API 统一响应包装
 *
 * @param <T> 响应数据类型
 */
@Data
public class FeishuApiResponse<T> {

    /**
     * 错误码，0 表示成功
     */
    private int code;

    /**
     * 错误消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code == 0;
    }
}
