FROM sbtscala/scala-sbt:eclipse-temurin-17.0.15_6_1.11.4_3.7.2 AS build

COPY --chown=sbtuser:sbtuser . /home/sbtuser/src
WORKDIR /home/sbtuser/src
RUN sbt app/assembly

FROM openjdk:17
EXPOSE 8080 8443
RUN mkdir /app /data /app-data
COPY --from=build /home/sbtuser/src/app/target/scala-*/simple-split-backend.jar /app/simplesplit-backend.jar
ENTRYPOINT ["java", "-jar", "/app/simplesplit-backend.jar"]
