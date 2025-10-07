FROM openjdk:21-jdk-slim

#RUN apk add --no-cache curl

RUN apt-get update && \
    apt-get install -y curl netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8888


ENTRYPOINT ["java", "-jar", "app.jar"]