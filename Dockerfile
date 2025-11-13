FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
ARG JAR_FILE=target/app.jar
COPY ${JAR_FILE} /app/app.jar
RUN adduser --disabled-password --gecos "" --home "/nonexistent" --shell "/sbin/nologin" --no-create-home --uid 10001 appuser
USER appuser
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
