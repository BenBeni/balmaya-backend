FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
ARG JAR_FILE=build/libs/balmaya-0.1.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]

