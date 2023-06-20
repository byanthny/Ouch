FROM gradle:7-jdk-jammy
WORKDIR /app
COPY . .
EXPOSE 7000
RUN gradle build
CMD ["gradle", "run"]
