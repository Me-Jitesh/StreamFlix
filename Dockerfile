FROM maven:3.9.0-eclipse-temurin-17-alpine AS build
COPY . .
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-jdk-alpine
RUN apk update && apk add --no-cache bash ffmpeg
MAINTAINER Jiteshs101@gmail.com
VOLUME /tmp
COPY  --from=build /target/*.jar /app.jar
ENTRYPOINT ["java","-jar","app.jar"]