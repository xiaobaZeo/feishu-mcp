# API 使用示例

本文档提供飞书 MCP 服务器的 API 调用示例。

## 基础信息

- **基础URL**: `http://localhost:8088`
- **MCP端点**: `/mcp`
- **健康检查**: `/health`

## MCP 协议调用

### 1. 初始化连接 (SSE)

```bash
curl -N http://localhost:8088/mcp
```

### 2. 获取工具列表

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/list",
    "id": "1"
  }'
```

### 3. 调用工具 - 搜索用户

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "2",
    "params": {
      "name": "user_search",
      "arguments": {
        "keyword": "张三",
        "page_size": 10,
        "page": 1
      }
    }
  }'
```

### 4. 调用工具 - 获取用户信息

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "3",
    "params": {
      "name": "user_info",
      "arguments": {
        "user_id": "ou_xxx"
      }
    }
  }'
```

### 5. 调用工具 - 搜索文档

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "4",
    "params": {
      "name": "doc_search",
      "arguments": {
        "query": "会议纪要",
        "page_size": 10
      }
    }
  }'
```

### 6. 调用工具 - 获取文档内容

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "5",
    "params": {
      "name": "doc_get",
      "arguments": {
        "document_id": "doxxxx"
      }
    }
  }'
```

### 7. 调用工具 - 创建文档

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "6",
    "params": {
      "name": "doc_create",
      "arguments": {
        "title": "新文档标题",
        "content": "这是文档内容"
      }
    }
  }'
```

### 8. 调用工具 - 获取文档评论

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "7",
    "params": {
      "name": "doc_comment_list",
      "arguments": {
        "document_id": "doxxxx"
      }
    }
  }'
```

### 9. 调用工具 - 添加文档评论

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "8",
    "params": {
      "name": "doc_comment_add",
      "arguments": {
        "document_id": "doxxxx",
        "content": "这是一条评论"
      }
    }
  }'
```

### 10. 调用工具 - 获取知识空间文档

```bash
curl -X POST http://localhost:8088/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "tools/call",
    "id": "9",
    "params": {
      "name": "knowledge_node_docs",
      "arguments": {
        "node_id": "nodexxx",
        "page_size": 20
      }
    }
  }'
```

## 监控端点

### 健康检查

```bash
curl http://localhost:8088/health
curl http://localhost:8088/ready
curl http://localhost:8088/actuator/health
```

### 应用信息

```bash
curl http://localhost:8088/actuator/info
```

### 指标数据

```bash
curl http://localhost:8088/actuator/metrics
curl http://localhost:8088/actuator/metrics/jvm.memory.used
curl http://localhost:8088/actuator/prometheus
```

## STDIO 模式

STDIO 模式用于命令行工具集成，通过标准输入输出进行通信：

```bash
echo '{"jsonrpc":"2.0","method":"tools/list","id":"1"}' | java -jar feishu-mcp-server-1.0.0.jar --mcp.server.transport=stdio
```