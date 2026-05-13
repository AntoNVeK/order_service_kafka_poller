FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/home/gradle/.gradle ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]