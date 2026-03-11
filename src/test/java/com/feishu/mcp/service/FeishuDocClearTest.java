package com.feishu.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feishu.mcp.config.FeishuProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

/**
 * 文档清空功能测试
 */
@SpringBootTest
public class FeishuDocClearTest {

    @Autowired
    private FeishuDocService feishuDocService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 测试清空指定文档内容
     * 文档URL: https://my.feishu.cn/wiki/RXwpwnibTi2uAEkosrpcGbS5nAb
     */
    @Test
    public void testClearDoc() throws Exception {
        String documentId = "RXwpwnibTi2uAEkosrpcGbS5nAb";

        System.out.println("========================================");
        System.out.println("开始清空文档: " + documentId);
        System.out.println("========================================");

        Map<String, Object> result = feishuDocService.clearDoc(documentId);

        System.out.println("\n清空结果:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));

        System.out.println("\n========================================");
        if (Boolean.TRUE.equals(result.get("success"))) {
            System.out.println("✅ 文档清空成功!");
            System.out.println("删除块数: " + result.get("deleted_count"));
        } else {
            System.out.println("❌ 文档清空失败!");
            System.out.println("错误: " + result.get("error"));
        }
        System.out.println("========================================");
    }
}
