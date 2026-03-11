package com.feishu.mcp.config;

import com.feishu.mcp.constant.McpConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP服务器配置
 */
@Component
@ConfigurationProperties(prefix = "mcp.server")
public class McpServerProperties {

    private String name = "feishu-mcp-server";
    private String version = "1.0.0";
    private String transport = McpConstants.TRANSPORT_BOTH;
    private HttpProperties http = new HttpProperties();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public HttpProperties getHttp() {
        return http;
    }

    public void setHttp(HttpProperties http) {
        this.http = http;
    }

    public static class HttpProperties {
        private String host = "0.0.0.0";
        private int port = 8088;
        private String path = "/mcp";

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}