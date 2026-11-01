# 后端镜像：使用宿主机已构建的 jar（先执行 mvn -DskipTests package）
# 构建上下文为仓库根目录（docker-compose.yml 中已配置）
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY blog-server/target/blog-server.jar app.jar

ENV TZ=Asia/Shanghai

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
