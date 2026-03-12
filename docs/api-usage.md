# API 使用

## 工具列表

```bash
curl http://localhost:8088/mcp/tools
```

## 调用工具示例（搜索用户）

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

## 健康检查

```bash
curl http://localhost:8088/health
curl http://localhost:8088/ready
```
