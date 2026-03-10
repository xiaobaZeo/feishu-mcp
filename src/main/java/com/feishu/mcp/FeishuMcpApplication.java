package com.feishu.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FeishuMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeishuMcpApplication.class, args);
    }
}