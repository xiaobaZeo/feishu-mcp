package com.feishu.mcp.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * JSON Schema 构建工具
 * 简化 Tool 类中 inputSchema 的定义
 */
public class SchemaBuilder {

    private final ObjectMapper mapper;
    private final ObjectNode schema;
    private final ObjectNode properties;
    private final ArrayNode required;

    public SchemaBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
        this.schema = mapper.createObjectNode();
        this.properties = mapper.createObjectNode();
        this.required = mapper.createArrayNode();
        schema.put("type", "object");
    }

    /**
     * 添加字符串类型参数
     */
    public SchemaBuilder addString(String name, String description) {
        return addString(name, description, false, null, null);
    }

    /**
     * 添加字符串类型参数（带必填标记）
     */
    public SchemaBuilder addString(String name, String description, boolean isRequired) {
        return addString(name, description, isRequired, null, null);
    }

    /**
     * 添加字符串类型参数（带默认值）
     */
    public SchemaBuilder addString(String name, String description, boolean isRequired, String defaultValue) {
        return addString(name, description, isRequired, defaultValue, null);
    }

    /**
     * 添加字符串类型参数（完整版）
     */
    public SchemaBuilder addString(String name, String description, boolean isRequired,
                                   String defaultValue, List<String> enumValues) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "string");
        prop.put("description", description);

        if (defaultValue != null) {
            prop.put("default", defaultValue);
        }

        if (enumValues != null && !enumValues.isEmpty()) {
            ArrayNode enumArray = mapper.createArrayNode();
            enumValues.forEach(enumArray::add);
            prop.set("enum", enumArray);
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 添加整数类型参数
     */
    public SchemaBuilder addInteger(String name, String description) {
        return addInteger(name, description, false, null);
    }

    /**
     * 添加整数类型参数（带必填标记）
     */
    public SchemaBuilder addInteger(String name, String description, boolean isRequired) {
        return addInteger(name, description, isRequired, null);
    }

    /**
     * 添加整数类型参数（完整版）
     */
    public SchemaBuilder addInteger(String name, String description, boolean isRequired, Integer defaultValue) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "integer");
        prop.put("description", description);

        if (defaultValue != null) {
            prop.put("default", defaultValue);
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 添加布尔类型参数
     */
    public SchemaBuilder addBoolean(String name, String description) {
        return addBoolean(name, description, false, null);
    }

    /**
     * 添加布尔类型参数（完整版）
     */
    public SchemaBuilder addBoolean(String name, String description, boolean isRequired, Boolean defaultValue) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "boolean");
        prop.put("description", description);

        if (defaultValue != null) {
            prop.put("default", defaultValue);
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 添加枚举类型参数
     */
    public SchemaBuilder addEnum(String name, String description, boolean isRequired,
                                  List<String> values, String defaultValue) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("枚举值不能为空");
        }

        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "string");
        prop.put("description", description);

        ArrayNode enumArray = mapper.createArrayNode();
        values.forEach(enumArray::add);
        prop.set("enum", enumArray);

        if (defaultValue != null) {
            prop.put("default", defaultValue);
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 添加对象类型参数
     */
    public SchemaBuilder addObject(String name, String description, boolean isRequired,
                                   ObjectNode objectSchema) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "object");
        prop.put("description", description);

        if (objectSchema != null) {
            // 复制 objectSchema 的内容
            objectSchema.fields().forEachRemaining(entry ->
                prop.set(entry.getKey(), entry.getValue())
            );
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 添加数组类型参数
     */
    public SchemaBuilder addArray(String name, String description, boolean isRequired,
                                  ObjectNode itemSchema) {
        ObjectNode prop = mapper.createObjectNode();
        prop.put("type", "array");
        prop.put("description", description);

        if (itemSchema != null) {
            prop.set("items", itemSchema);
        }

        properties.set(name, prop);

        if (isRequired) {
            required.add(name);
        }
        return this;
    }

    /**
     * 构建最终的 Schema
     */
    public JsonNode build() {
        schema.set("properties", properties);
        if (required.size() > 0) {
            schema.set("required", required);
        }
        return schema;
    }
}
