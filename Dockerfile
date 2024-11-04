FROM amazoncorretto:21
COPY build/libs/insights-0.0.1-SNAPSHOT.jar insights.jar
ENTRYPOINT ["java", "-jar", "insights.jar"]