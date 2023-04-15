#This is just a sample Docker build file for the application. Feel free to make changes in order to suit your needs.

FROM openjdk:17-alpine

ARG SBT_VERSION=1.7.2
ARG SCALA_VERSION=2.12.16
ENV SCALA_HOME=/usr/share/scala

# Update Alpine Linux Package Manager and Install the bash
RUN apk update && apk add bash

RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
  apk add --no-cache bash && \
  cd "/tmp" && \
  wget "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
  tar xzf "scala-${SCALA_VERSION}.tgz" && \
  mkdir "${SCALA_HOME}" && \
  rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
  mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
  ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
  apk del .build-dependencies && \
  rm -rf "/tmp/"*


RUN \
  apk add --no-cache --virtual=.build-dependencies bash curl bc ca-certificates && \
  cd "/tmp" && \
  update-ca-certificates && \
  curl -fsL https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz | tar xfz - -C /usr/local && \
  $(mv /usr/local/sbt-launcher-packaging-$SBT_VERSION /usr/local/sbt || true) && \
  ln -s /usr/local/sbt/bin/* /usr/local/bin/ && \
  apk del .build-dependencies && \
  rm -rf "/tmp/"*


COPY . /root
WORKDIR /root

RUN \
  sbt build -Dsbt.rootdir=true

RUN echo '#!/bin/ash' >> /root/run.sh
RUN echo 'java -jar /root/modules/application/target/scala-2.13/zio-microservice-seed.jar' >> /root/run.sh
RUN chmod a+x /root/run.sh

EXPOSE 8080

CMD [ "/root/run.sh" ]
