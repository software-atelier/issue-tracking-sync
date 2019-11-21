FROM  gradle:6.0-jdk8 AS build
COPY --chown=gradle:gradle . /home/gradle/src
ARG MVN_REPO
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --stacktrace -P repositoryIssueTrackingJars=${MVN_REPO}

FROM openjdk:8
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
COPY --from=build /home/gradle/src/settings.json settings.json
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
