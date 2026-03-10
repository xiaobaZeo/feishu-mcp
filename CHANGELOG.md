# 更新日志

所有项目的显著变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
并且本项目遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [未发布]

### 新增
- 实现10个MCP工具：用户搜索、用户信息、文件内容、文档搜索、文档创建、文档查看、文档更新、知识空间文档、文档评论列表、添加评论
- 支持STDIO和SSE两种通信模式
- 支持应用凭证和用户授权两种认证方式
- 添加全局异常处理
- 添加健康检查端点
- 添加Docker支持
- 添加CI/CD配置（GitHub Actions）
- 添加单元测试和集成测试
- 添加CLAUDE.md项目指南

### 技术栈
- Spring Boot 3.2.5
- JDK 21
- Spring WebFlux (SSE)
- OkHttp (HTTP客户端)
- Jackson (JSON处理)

## [1.0.0] - 2026-03-10

### 新增
- 初始版本发布
- 基础MCP协议实现（JSON-RPC 2.0）
- 飞书API集成（用户、文件、文档、知识空间）
- Spring Boot应用框架