#This is just a sample Docker build file for the application. Feel free to make changes in order to suit your needs.

FROM openjdk:17-alpine

ARG SBT_VERSION=1.7.2

# Update Alpine Linux Package Manager and Install the bash
RUN apk update && apk add bash

RUN \
  apk add --no-cache --virtual=.build-dependencies bash curl bc ca-certificates && \
  cd "/tmp" && \
  update-ca-certificates && \
  curl -fsL https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar xfz - -C /usr/local && \
  $(mv /usr/local/sbt-launcher-packaging-$SBT_VERSION /usr/local/sbt || true) && \
  ln -s /usr/local/sbt/bin/* /usr/local/bin/ && \
  apk del .build-dependencies && \
  rm -rf "/tmp/"*

WORKDIR /app
EXPOSE 8080
COPY . /app

CMD [ "sbt", "run", "-Dsbt.rootdir=true"]