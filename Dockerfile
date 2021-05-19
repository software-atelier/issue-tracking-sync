FROM  gradle:6.0-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
# tag::parameters[]
ARG MVN_REPO
# end::parameters[]
WORKDIR /home/gradle/src
RUN gradle build --no-daemon --stacktrace -P repositoryIssueTrackingJars=${MVN_REPO}

FROM openjdk:11
# tag::parameters[]
ARG SETTINGSFILE=test/resources/settings.json
ARG SETTINGSTARGET=test/settings.yml
ARG APPLICATIONFILE=test/resources/application-test.yml
# end::parameters[]
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
COPY ${APPLICATIONFILE} config/application.yml
COPY ${SETTINGSFILE} ${SETTINGSTARGET}
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
