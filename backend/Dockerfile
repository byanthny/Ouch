# Build
FROM gradle:8.1.1-jdk17 AS BUILD_STAGE
WORKDIR /build
COPY . .
RUN gradle build --no-daemon || return 1

# Exec
FROM openjdk:17

ENV DIR=/app
ENV JAR=Ouch-all.jar

WORKDIR $DIR
COPY --from=BUILD_STAGE /build/$JAR $DIR/

EXPOSE 7000
ENTRYPOINT exec java -jar $JAR
