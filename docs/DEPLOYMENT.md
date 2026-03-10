# 部署指南

本文档介绍飞书 MCP 服务器的多种部署方式。

## 目录

1. [本地部署](#本地部署)
2. [Docker 部署](#docker-部署)
3. [Docker Compose 部署](#docker-compose-部署)
4. [生产环境部署建议](#生产环境部署建议)
5. [监控和日志](#监控和日志)

## 本地部署

### 前置要求

- JDK 21+
- Maven 3.9+
- 飞书开放平台应用凭证

### 步骤

1. **克隆项目**

```bash
git clone <repository-url>
cd feishu-mcp-server-jdk21
```

2. **编译打包**

```bash
mvn clean package -DskipTests
```

3. **配置环境变量**

```bash
export FEISHU_APP_ID=your_app_id
export FEISHU_APP_SECRET=your_app_secret
export FEISHU_AUTH_MODE=app
export MCP_SERVER_TRANSPORT=both
```

4. **运行应用**

```bash
# 开发环境
java -jar target/feishu-mcp-server-1.0.0.jar --spring.profiles.active=dev

# 生产环境
java -jar target/feishu-mcp-server-1.0.0.jar --spring.profiles.active=prod
```

## Docker 部署

### 前置要求

- Docker 20.10+

### 步骤

1. **构建镜像**

```bash
docker build -t feishu-mcp-server:latest .
```

2. **运行容器**

```bash
docker run -d \
  --name feishu-mcp-server \
  -p 8088:8088 \
  -e FEISHU_APP_ID=your_app_id \
  -e FEISHU_APP_SECRET=your_app_secret \
  -e FEISHU_AUTH_MODE=app \
  -e MCP_SERVER_TRANSPORT=both \
  --restart unless-stopped \
  feishu-mcp-server:latest
```

3. **查看日志**

```bash
docker logs -f feishu-mcp-server
```

4. **停止容器**

```bash
docker stop feishu-mcp-server
docker rm feishu-mcp-server
```

## Docker Compose 部署

### 步骤

1. **复制环境变量模板**

```bash
cp .env.example .env
```

2. **编辑 .env 文件**

```bash
FEISHU_APP_ID=your_app_id
FEISHU_APP_SECRET=your_app_secret
FEISHU_AUTH_MODE=app
MCP_SERVER_TRANSPORT=both
```

3. **启动服务**

```bash
docker-compose up -d
```

4. **查看状态**

```bash
docker-compose ps
docker-compose logs -f
```

5. **停止服务**

```bash
docker-compose down
```

### 更新部署

```bash
# 拉取最新代码
git pull origin main

# 重新构建并启动
docker-compose up -d --build
```

## 生产环境部署建议

### 1. 使用反向代理

推荐使用 Nginx 或 Traefik 作为反向代理：

```nginx
server {
    listen 80;
    server_name mcp.example.com;

    location / {
        proxy_pass http://localhost:8088;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;

        # SSE 支持
        proxy_buffering off;
        proxy_read_timeout 86400;
    }
}
```

### 2. 使用 systemd 管理

创建 `/etc/systemd/system/feishu-mcp-server.service`：

```ini
[Unit]
Description=Feishu MCP Server
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/feishu-mcp-server
ExecStart=/usr/bin/java -jar feishu-mcp-server-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
Environment="FEISHU_APP_ID=your_app_id"
Environment="FEISHU_APP_SECRET=your_app_secret"

[Install]
WantedBy=multi-user.target
```

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable feishu-mcp-server
sudo systemctl start feishu-mcp-server
sudo systemctl status feishu-mcp-server
```

### 3. JVM 参数优化

```bash
java \
  -Xms512m \
  -Xmx2g \
  -XX:+UseG1GC \
  -XX:+UseStringDeduplication \
  -jar feishu-mcp-server-1.0.0.jar \
  --spring.profiles.active=prod
```

### 4. 安全配置

- 使用防火墙限制端口访问
- 启用 HTTPS
- 定期更新依赖
- 监控安全公告

## 监控和日志

### 健康检查

```bash
# 应用健康
curl http://localhost:8088/actuator/health

# 就绪检查
curl http://localhost:8088/ready

# 详细信息
curl http://localhost:8088/actuator/info
```

### 指标监控

Prometheus 指标端点：`http://localhost:8088/actuator/prometheus`

Grafana 仪表板配置示例在 `monitoring/grafana-dashboard.json`

### 日志配置

日志文件位置：`/var/log/feishu-mcp-server/`

日志轮转配置（logrotate）：

```
/var/log/feishu-mcp-server/*.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 appuser appuser
}
```

## 故障排查

### 常见问题

1. **启动失败**
   - 检查环境变量配置
   - 检查端口是否被占用
   - 查看日志：`docker logs feishu-mcp-server`

2. **飞书API调用失败**
   - 检查应用权限
   - 检查网络连接
   - 确认 token 未过期

3. **SSE连接断开**
   - 检查反向代理配置
   - 确认超时设置
   - 查看应用日志

### 联系支持

如有问题，请提交 GitHub Issue。