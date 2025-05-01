FROM eclipse-temurin:21 as build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN ./gradlew
RUN ./gradlew build -x test --no-daemon

COPY src src

RUN ./gradlew bootJar -x test --no-daemon \
 && JAR_FILE=$(ls build/libs/*.jar | head -n 1) \
 && echo "Detected jar: $JAR_FILE" \
 && cp "$JAR_FILE" app.jar


FROM eclipse-temurin:21-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
