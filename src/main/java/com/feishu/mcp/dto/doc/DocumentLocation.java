package com.feishu.mcp.dto.doc;

import com.feishu.mcp.constant.DocBlockType;
import lombok.Builder;
import lombok.Data;

/**
 * 文档定位 DTO
 * 支持多种定位方式，优先级从高到低：
 * 1. block_id - 直接指定块ID
 * 2. block_index - 按块索引（0-based）
 * 3. block_type - 按块类型（如 HEADING_1）
 * 4. position_hint - 按位置描述
 */
@Data
@Builder
public class DocumentLocation {

    /**
     * 块ID（最高优先级）
     */
    private String blockId;

    /**
     * 块索引（从0开始）
     */
    private Integer blockIndex;

    /**
     * 块类型（如 TEXT, HEADING_1 等）
     */
    private DocBlockType blockType;

    /**
     * 位置描述（如"第一段之后"）
     */
    private String positionHint;

    /**
     * 判断是否有有效的定位信息
     */
    public boolean isValid() {
        return blockId != null && !blockId.isEmpty()
                || blockIndex != null
                || blockType != null
                || positionHint != null && !positionHint.isEmpty();
    }

    /**
     * 判断是否直接指定了块ID
     */
    public boolean hasBlockId() {
        return blockId != null && !blockId.isEmpty();
    }

    /**
     * 判断是否使用索引定位
     */
    public boolean hasBlockIndex() {
        return blockIndex != null;
    }

    /**
     * 判断是否使用类型定位
     */
    public boolean hasBlockType() {
        return blockType != null;
    }

    /**
     * 判断是否使用位置描述定位
     */
    public boolean hasPositionHint() {
        return positionHint != null && !positionHint.isEmpty();
    }

    /**
     * 获取最高优先级的定位方式描述
     */
    public String getLocationDescription() {
        if (hasBlockId()) {
            return "block_id=" + blockId;
        }
        if (hasBlockIndex()) {
            return "block_index=" + blockIndex;
        }
        if (hasBlockType()) {
            return "block_type=" + blockType;
        }
        if (hasPositionHint()) {
            return "position_hint=" + positionHint;
        }
        return "none";
    }
}
