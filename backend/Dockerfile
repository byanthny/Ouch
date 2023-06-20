FROM gradle:7-jdk-jammy
WORKDIR /app
COPY . .
EXPOSE $PORT
RUN gradle build
CMD ["gradle", "run"]
