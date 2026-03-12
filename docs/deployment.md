# 部署方式

## 方式一：本地部署

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

## 方式二：Docker 部署

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

## 方式三：Docker Compose 部署（推荐）

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
