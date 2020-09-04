FROM openjdk:14
ADD ./target/clock-1.0.0.jar /bin
WORKDIR /bin
ENTRYPOINT ["java", "-jar", "/bin/clock-1.0.0.jar"]
