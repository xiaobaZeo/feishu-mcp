# MCP 客户端配置

将飞书 MCP 服务器集成到各种 AI 编程助手和 MCP 客户端。

## Claude Code CLI

支持 **STDIO 模式**（本地启动）和 **HTTP 模式**（连接远程服务）。

### STDIO 模式（推荐）

编辑 `~/.claude/settings.json`：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

### HTTP 模式（连接已运行的服务）

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "url": "http://localhost:8088/mcp"
    }
  }
}
```

---

## Cursor

Cursor 仅支持 **STDIO 模式**。

### 方式一：图形界面配置
1. 打开 `Settings` → `Features` → `MCP`
2. 点击 `Add MCP Server`
3. 选择 `command` 类型
4. 填写配置信息

### 方式二：配置文件

编辑 `~/.cursor/mcp.json`：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

---

## Windsurf

Windsurf 仅支持 **STDIO 模式**。

编辑 `~/.windsurf/mcp_config.json`：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

---

## Cline (VS Code 插件)

Cline 支持 **STDIO 模式** 和 **HTTP 模式**。

### STDIO 模式（推荐）

在 VS Code 的 Cline 插件设置中配置：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

### HTTP 模式（连接已运行的服务）

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "url": "http://localhost:8088/mcp"
    }
  }
}
```

---

## Google Gemini CLI

Gemini CLI 仅支持 **STDIO 模式**。

编辑 `~/.gemini/mcp_config.json`：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

---

## MCP Inspector

[MCP Inspector](https://github.com/modelcontextprotocol/inspector) 是官方提供的 MCP 服务器调试工具，支持 **STDIO 模式** 和 **HTTP 模式**。

**安装：**
```bash
npm install -g @modelcontextprotocol/inspector
```

**STDIO 模式**（本地启动服务器）
```bash
npx @modelcontextprotocol/inspector java -jar /path/to/feishu-mcp-server-1.0.0.jar --mcp.server.transport=stdio
```

**HTTP 模式**（连接已运行的服务）
```bash
npx @modelcontextprotocol/inspector --url http://localhost:8088/mcp
```

**环境变量设置（可选）：**
```bash
export FEISHU_APP_ID=cli_xxxxxxxxxxxxxxxx
export FEISHU_APP_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
npx @modelcontextprotocol/inspector java -jar /path/to/feishu-mcp-server-1.0.0.jar --mcp.server.transport=stdio
```

---

## OpenClaw (通过 MCPorter)

[OpenClaw](https://github.com/pomdtr/openclaw) 是一个跨平台的 MCP 客户端，仅支持 **STDIO 模式**。

**安装 MCPorter：**
```bash
npm install -g mcporter
```

**配置 MCPorter：**

编辑 `~/.config/mcporter/config.json`：

```json
{
  "mcpServers": {
    "feishu-mcp": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/path/to/feishu-mcp-server-1.0.0.jar",
        "--mcp.server.transport=stdio"
      ],
      "env": {
        "FEISHU_APP_ID": "cli_xxxxxxxxxxxxxxxx",
        "FEISHU_APP_SECRET": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
        "FEISHU_AUTH_MODE": "app"
      }
    }
  }
}
```

**启动 OpenClaw：**
```bash
mcporter start
```

---

## 配置说明

| 模式 | 适用场景 | 说明 |
|------|----------|------|
| **STDIO** | 本地开发、AI 编程助手 | 客户端直接启动 MCP 服务器进程，通过标准输入输出通信 |
| **HTTP** | Docker 部署、远程服务 | 通过 HTTP 请求与已运行的服务通信，适合常驻服务或多客户端共享 |

**通用配置项：**
- 将 `/path/to/` 替换为实际的 JAR 文件路径
- 将 `FEISHU_APP_ID` 和 `FEISHU_APP_SECRET` 替换为你的飞书应用凭证
