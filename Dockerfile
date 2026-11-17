# Stage 1: Build application
FROM maven:3.9.9-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build artifact
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Lean runtime
FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# Run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /build/target/securepay-wallet-api-*.jar app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/v3/api-docs > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
