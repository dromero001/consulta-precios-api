FROM maven:3.9.16-eclipse-temurin-17@sha256:44512abb01061e282da7a8f86fd3c8e50609538ff9644cdb132e1ff9fccdaecb AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17.0.20_8-jre-alpine@sha256:45d19def67191d1df7d233282051ac2f6908c6b2d7eaef75441d7e7c721c6d01
RUN addgroup -S app && adduser -S app -G app
USER app
WORKDIR /app
COPY --from=build /workspace/target/consulta-precios-api-*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
