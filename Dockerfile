# Use an official OpenJDK runtime as a base image
FROM maven:3.8.4-openjdk-11

# Set the working directory in the container
WORKDIR /app

# Copy the project files into the container
COPY . /app

WORKDIR /app/project

# Run Maven clean install inside the project directory
RUN mvn clean install

# Set the working directory to the project directory
WORKDIR /app/jbpm-spring-boot-service

# Run Maven clean install inside the project directory

# Reset the working directory back to /app
WORKDIR /app
RUN mvn clean install


# Copy the executable JAR file from the target directory into the container
COPY ./target/jbpm-spring-boot-service-1.0-SNAPSHOT.jar /app/jbpm-spring-boot-service-1.0-SNAPSHOT.jar

# Expose the port your application runs on
EXPOSE 8090

# Define the command to run your application
CMD ["java", "-jar", "jbpm-spring-boot-service-1.0-SNAPSHOT.jar"]
