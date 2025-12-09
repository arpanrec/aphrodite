# syntax=docker/dockerfile:experimental
ARG JAVA_VERSION=21
FROM eclipse-temurin:${JAVA_VERSION} AS builder

WORKDIR /app

COPY . /app/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :aphrodite-backend:clean :aphrodite-backend:bootJar --stacktrace --info -x test

FROM eclipse-temurin:${JAVA_VERSION}-jre
WORKDIR /app
#COPY --from=builder /app/aphrodite-backend/build/libs/aphrodite-backend-boot-* /app.jar

LABEL org.opencontainers.image.source=git@github.com:arpanrec/aphrodite.git
LABEL org.opencontainers.image.description="Aphrodite Application Image"
LABEL org.opencontainers.image.licenses=WTFPL

ADD aphrodite-backend/build/libs/aphrodite-backend-boot-*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
