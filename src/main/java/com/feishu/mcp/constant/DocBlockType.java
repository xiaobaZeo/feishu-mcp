package com.feishu.mcp.constant;

import java.util.Arrays;

/**
 * 飞书文档块类型枚举
 * 参考飞书文档API：https://open.feishu.cn/document/uAjLw4CM
 */
public enum DocBlockType {

    // 页面块（根块）
    PAGE(1, "页面", false),

    // 文本块
    TEXT(2, "文本", true),

    // 标题块
    HEADING_1(3, "一级标题", true),
    HEADING_2(4, "二级标题", true),
    HEADING_3(5, "三级标题", true),
    HEADING_4(6, "四级标题", true),
    HEADING_5(7, "五级标题", true),
    HEADING_6(8, "六级标题", true),
    HEADING_7(9, "七级标题", true),
    HEADING_8(10, "八级标题", true),
    HEADING_9(11, "九级标题", true),

    // 列表块
    BULLETED_LIST(12, "无序列表", true),
    NUMBERED_LIST(13, "有序列表", true),

    // 特殊文本块
    CODE(14, "代码块", true),
    QUOTE(15, "引用", true),

    // 待办
    TODO(17, "待办", true),

    // 表格
    TABLE(18, "表格", false),

    // 多媒体
    IMAGE(27, "图片", false),
    FILE(28, "文件", false),
    NESTED_IMAGE(29, "嵌套图片", false);

    private final int code;
    private final String description;
    private final boolean textEditable;

    DocBlockType(int code, String description, boolean textEditable) {
        this.code = code;
        this.description = description;
        this.textEditable = textEditable;
    }

    /**
     * 获取块类型代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取块类型描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 是否支持文本编辑
     */
    public boolean isTextEditable() {
        return textEditable;
    }

    /**
     * 根据代码查找块类型
     */
    public static DocBlockType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据字符串名称查找块类型
     * 支持：text, heading1, heading2, bulleted_list, numbered_list 等
     */
    public static DocBlockType fromString(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }

        String normalized = type.toUpperCase().replace("-", "_");

        // 特殊处理 headingX 格式
        if (normalized.startsWith("HEADING")) {
            try {
                int level = Integer.parseInt(normalized.replace("HEADING", ""));
                if (level >= 1 && level <= 9) {
                    return Arrays.stream(values())
                            .filter(t -> t.name().equals("HEADING_" + level))
                            .findFirst()
                            .orElse(null);
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误
            }
        }

        // 标准枚举名称匹配
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // 尝试匹配描述
            return Arrays.stream(values())
                    .filter(t -> t.description.equals(type))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 判断是否为标题类型
     */
    public boolean isHeading() {
        return this.name().startsWith("HEADING_");
    }

    /**
     * 判断是否为列表类型
     */
    public boolean isList() {
        return this == BULLETED_LIST || this == NUMBERED_LIST;
    }

    /**
     * 获取所有可编辑的块类型
     */
    public static DocBlockType[] getEditableTypes() {
        return Arrays.stream(values())
                .filter(DocBlockType::isTextEditable)
                .toArray(DocBlockType[]::new);
    }
}
