FROM openjdk:17-jdk-slim

COPY /build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]

