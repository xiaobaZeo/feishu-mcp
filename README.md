# 飞书 MCP 服务器

基于 Spring Boot 3.x + JDK 21 的飞书 MCP (Model Context Protocol) 服务器，支持 STDIO 和 SSE 两种通信模式。

## 功能特性

1. **用户搜索** - 根据关键词搜索企业内的用户（ID、姓名、头像）
2. **用户信息** - 获取用户个人信息（本人或其他用户）
3. **文件内容** - 根据 file_token 获取文件二进制内容
4. **文档搜索** - 根据关键词、创建者等条件搜索云文档
5. **文档创建** - 在我的文档库或指定知识空间节点下创建云文档
6. **文档查看** - 获取云文档的完整内容
7. **文档更新** - 在指定位置增加或替换内容
8. **知识空间文档** - 获取指定节点下的云文档列表（支持分页）
9. **文档评论** - 查看文档的全文评论和划词评论
10. **添加评论** - 在文档中添加全文评论或划词评论

## 部署方式

### 方式一：本地部署

**环境要求：**
- JDK 21+
- Maven 3.9+
- 飞书开放平台应用（需申请相关权限）

**步骤：**

```bash
# 1. 克隆代码
git clone <repository-url>
cd feishu-mcp-server-jdk21

# 2. 编译打包
mvn clean package -DskipTests

# 3. 配置环境变量
export FEISHU_APP_ID=your_app_id
export FEISHU_APP_SECRET=your_app_secret
export FEISHU_AUTH_MODE=app
export MCP_SERVER_TRANSPORT=both

# 4. 运行
java -jar target/feishu-mcp-server-1.0.0.jar
# 服务将在 http://localhost:8088 启动
```

### 方式二：Docker 部署

**步骤：**

```bash
# 1. 编译打包
mvn clean package -DskipTests

# 2. 构建镜像
docker build -t feishu-mcp-server .

# 3. 运行容器
docker run -d \
  --name feishu-mcp-server \
  -p 8088:8088 \
  -e FEISHU_APP_ID=your_app_id \
  -e FEISHU_APP_SECRET=your_app_secret \
  -e FEISHU_AUTH_MODE=app \
  --restart unless-stopped \
  feishu-mcp-server

# 查看日志
docker logs -f feishu-mcp-server

# 停止容器
docker stop feishu-mcp-server
docker rm feishu-mcp-server
```

### 方式三：Docker Compose 部署（推荐）

**步骤：**

```bash
# 1. 克隆代码
git clone <repository-url>
cd feishu-mcp-server-jdk21

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 文件，填入你的飞书应用配置
# FEISHU_APP_ID=cli_xxxxxxxxxxxxxxxx
# FEISHU_APP_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# 3. 编译打包
mvn clean package -DskipTests

# 4. 启动服务
docker-compose up -d

# 5. 查看日志
docker-compose logs -f

# 6. 停止服务
docker-compose down
```

**docker-compose.yml 配置：**

```yaml
version: '3.8'

services:
  feishu-mcp-server:
    build: .
    image: feishu-mcp-server:latest
    container_name: feishu-mcp-server
    ports:
      - "8088:8088"
    environment:
      - FEISHU_APP_ID=${FEISHU_APP_ID}
      - FEISHU_APP_SECRET=${FEISHU_APP_SECRET}
      - FEISHU_AUTH_MODE=${FEISHU_AUTH_MODE:-app}
      - MCP_SERVER_TRANSPORT=${MCP_SERVER_TRANSPORT:-both}
    restart: unless-stopped
```

## 飞书权限申请

需要在 [飞书开放平台](https://open.feishu.cn/) 申请以下权限：

- `contact:user.id` - 获取用户 ID
- `contact:user.base` - 获取用户基本信息
- `drive:drive` - 云空间文件权限
- `docx:docx` - 云文档权限
- `knowledge:space` - 知识库权限

## API 使用

### 工具列表

```bash
curl http://localhost:8088/mcp/tools
```

### 调用工具示例（搜索用户）

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "1",
    "params": {
      "name": "user_search",
      "arguments": {
        "keyword": "张三"
      }
    }
  }'
```

### 健康检查

```bash
curl http://localhost:8088/health
curl http://localhost:8088/ready
```

## 可用工具

| 工具名 | 功能 |
|--------|------|
| `user_search` | 搜索企业用户 |
| `user_info` | 获取用户信息 |
| `file_content` | 获取文件内容 |
| `doc_search` | 搜索云文档 |
| `doc_create` | 创建云文档 |
| `doc_get` | 查看云文档 |
| `doc_update` | 更新云文档 |
| `knowledge_node_docs` | 知识空间文档列表 |
| `doc_comment_list` | 文档评论列表 |
| `doc_comment_add` | 添加评论 |

## 技术栈

- Spring Boot 3.2.x
- JDK 21
- Spring WebFlux (SSE)
- Jackson (JSON 处理)
- OkHttp (HTTP 客户端)

## 许可证

MIT