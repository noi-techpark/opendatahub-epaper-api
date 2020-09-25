FROM maven:3-jdk-8 as base

RUN mkdir -p /code
WORKDIR /code

# DEV stage ###################################################################
FROM base AS dev

ARG JENKINS_GROUP_ID=1000
ARG JENKINS_USER_ID=1000

RUN groupadd --gid $JENKINS_GROUP_ID jenkins && \
    useradd --uid $JENKINS_USER_ID --gid $JENKINS_GROUP_ID --create-home jenkins

# Not for BASE, because we do not need maven package caches for production
COPY infrastructure/docker/entrypoint-java.sh /entrypoint-java.sh
ENTRYPOINT [ "/entrypoint-java.sh" ]

# BUILD stage #################################################################
FROM base as build

COPY src /code/src
COPY pom.xml /code/pom.xml
