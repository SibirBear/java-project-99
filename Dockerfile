FROM eclipse-temurin:20-jdk

WORKDIR /app

COPY /. .

RUN ./gradlew --no-daemon dependencies

RUN ./gradlew --no-daemon build

ENV JAVA_OPTS "-Xmx512M -Xms512M"

CMD java -jar build/libs/app-0.0.1-SNAPSHOT.jar

EXPOSE 8080