package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 文本样式 DTO
 */
@Data
@Builder
public class TextStyle {

    /**
     * 是否加粗
     */
    private Boolean bold;

    /**
     * 是否斜体
     */
    private Boolean italic;

    /**
     * 是否下划线
     */
    private Boolean underline;

    /**
     * 是否删除线
     */
    private Boolean strikethrough;

    /**
     * 是否代码
     */
    private Boolean code;

    /**
     * 文字颜色
     */
    private String color;

    /**
     * 背景颜色
     */
    private String backgroundColor;

    /**
     * 字体大小
     */
    private Integer fontSize;

    /**
     * 粗体样式
     */
    public static TextStyle bold() {
        return TextStyle.builder().bold(true).build();
    }

    /**
     * 斜体样式
     */
    public static TextStyle italic() {
        return TextStyle.builder().italic(true).build();
    }

    /**
     * 粗斜体样式
     */
    public static TextStyle boldItalic() {
        return TextStyle.builder().bold(true).italic(true).build();
    }

    /**
     * 代码样式
     */
    public static TextStyle code() {
        return TextStyle.builder().code(true).build();
    }
}
