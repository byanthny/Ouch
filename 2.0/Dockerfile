# syntax = docker/dockerfile:1.2
# use JDK17
FROM gradle:8.1.1-jdk17
WORKDIR /app
# Only copy files needed to build, nothing else that gradle might try to use
# This prevents a weird possible bug when building the frontend
COPY . .
EXPOSE 7000
ARG SQL_URI
ARG SQL_USER
ARG SQL_PASSWORD
ENV SQL_URI=$SQL_URI
ENV SQL_USER=$SQL_USER
ENV SQL_PASSWORD=$SQL_PASSWORD
RUN gradle clean compileKotlinJvm -x compileKotlinJs -x allTests || exit 1
CMD ["gradle", "run", "--console=plain"]
