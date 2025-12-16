#use the official OpenJDK 17 image as the base image
FROM openjdk:17-jdk-alpine

#set the working directory inside the container
WORKDIR /app

#copy the Gradle build files
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

#Copy the source code
COPY src ./src

#Give execution permissions to the Gradle Wrapper
RUN chmod +X gradlew

#build the project
RUN ./gradlew build

#Expose the port your application will run on
EXPOSE 8080

#Run the application
ENTRYPOINT ["java", "-jar", "build/libs/"BlogApp-0.0.1-SNAPSHOT.jar"]