FROM gradle:8.5.0-jdk21 AS build
WORKDIR /project
COPY . /project/
RUN gradle assemble --info --no-daemon

FROM azul/zulu-openjdk:21.0.2-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /project/build/libs/*-fat.jar /app/rinha.jar
ENTRYPOINT ["java", "-Xms100m", "-Xmx230m", "-jar", "/app/rinha.jar"]