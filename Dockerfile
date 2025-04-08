FROM eclipse-temurin:17-jdk
COPY build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","/app.jar"]
