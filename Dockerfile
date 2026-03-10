FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制 jar 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8088

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8088/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]