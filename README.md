# 飞书 MCP 服务器

基于 Spring Boot 3.x + JDK 21 的飞书 MCP (Model Context Protocol) 服务器，支持 STDIO 和 HTTP 两种通信模式。

## 功能特性

### 双模式通信架构
- **STDIO 模式** - 本地进程通信，适合 Claude Code、Cursor 等客户端直接启动 JAR
- **HTTP 模式** - HTTP 远程连接，适合 Docker 部署或常驻服务
- **自动工具发现** - Spring 自动注入所有 MCP 工具，无需手动注册

### 用户管理
- **用户搜索** - 根据关键词模糊搜索企业成员（支持返回 ID、姓名、头像、邮箱等）
- **用户信息** - 查询指定用户的详细信息或获取当前登录用户信息

### 云文档管理
- **文档搜索** - 按关键词、创建者、时间范围等条件检索云文档
- **文档创建** - 在「我的文档库」或指定知识空间节点下创建 docx 文档
- **文档查看** - 获取云文档完整内容（支持块级结构解析）
- **文档更新** - 在指定位置追加或替换内容，支持 Markdown 格式
- **文档清空** - 一键清空文档所有内容（保留文档本身）

### 评论互动
- **评论列表** - 查看文档的全文评论和划词评论
- **添加评论** - 在文档中添加全文评论或针对选定文本添加划词评论

## 部署方式

详细部署说明请参考：[部署方式文档](./docs/deployment.md)

- 本地部署（JDK 21 + Maven）
- Docker 部署
- Docker Compose 部署（推荐）

## 飞书权限申请

详细权限说明请参考：[飞书权限申请文档](./docs/permissions.md)

## API 使用

详细 API 使用说明请参考：[API 使用文档](./docs/api-usage.md)

- 工具列表查询
- 调用工具示例
- 健康检查接口

## MCP 客户端配置

详细客户端配置说明请参考：[MCP 客户端配置文档](./docs/mcp-client-config.md)

支持以下客户端：
- Claude Code CLI
- Cursor
- Windsurf
- Cline (VS Code 插件)
- Google Gemini CLI
- MCP Inspector
- OpenClaw (通过 MCPorter)

## 可用工具

| 工具名 | 功能 |
|--------|------|
| `user_search` | 搜索企业用户 |
| `user_info` | 获取用户信息 |
| `doc_search` | 搜索云文档 |
| `doc_create` | 创建云文档 |
| `doc_get` | 查看云文档 |
| `doc_update` | 更新云文档 |
| `doc_comment_list` | 文档评论列表 |
| `doc_comment_add` | 添加评论 |
| `doc_clear` | 清空文档内容（保留文档） |

## 技术栈

- Spring Boot 3.2.x
- JDK 21
- Spring Web (HTTP)
- Jackson (JSON 处理)
- OkHttp (HTTP 客户端)


## Star History

[![Star History Chart](https://api.star-history.com/image?repos=xiaobaZeo/feishu-mcp&type=date&legend=top-left)](https://www.star-history.com/?repos=xiaobaZeo%2Ffeishu-mcp&type=date&legend=top-left)