package com.feishu.mcp.dto.doc;

import lombok.Builder;
import lombok.Data;

/**
 * 文档元素 DTO
 * 用于表示文档中的文本、样式等元素
 */
@Data
@Builder
public class DocElement {

    /**
     * 元素类型：text、image、file 等
     */
    private String tag;

    /**
     * 文本内容（text 类型时使用）
     */
    private String text;

    /**
     * 文本样式
     */
    private TextStyle style;

    /**
     * 文件 token（image/file 类型时使用）
     */
    private String fileToken;

    /**
     * 创建纯文本元素
     */
    public static DocElement text(String text) {
        return DocElement.builder()
                .tag("text")
                .text(text)
                .build();
    }

    /**
     * 创建带样式的文本元素
     */
    public static DocElement text(String text, TextStyle style) {
        return DocElement.builder()
                .tag("text")
                .text(text)
                .style(style)
                .build();
    }
}
