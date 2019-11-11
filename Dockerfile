FROM anapsix/alpine-java:8
ARG JARPATH=./build/libs/
ARG JARFILE=issue-tracking-sync-1.0-SNAPSHOT.jar
COPY ${JARPATH}${JARFILE} app.jar
COPY settings.json settings.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
