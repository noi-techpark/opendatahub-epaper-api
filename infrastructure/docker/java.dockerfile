FROM maven:3-jdk-8 as base

RUN mkdir -p /code
WORKDIR /code

# DEV stage ###################################################################
FROM base AS dev

# Not for BASE, because we do not need maven package caches for production
COPY infrastructure/docker/entrypoint-java.sh /entrypoint-java.sh
RUN chmod +x /entrypoint-java.sh

# BUILD stage #################################################################
FROM base as build

COPY src /code/src
COPY pom.xml /code/pom.xml

# Set the entrypoint for the container
ENTRYPOINT [ "/entrypoint-java.sh" ]
