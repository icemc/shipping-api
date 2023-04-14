#This is just a sample Docker build file for the application. Feel free to make changes in order to suit your needs.

FROM openjdk:17-alpine

ARG SCALA_VERSION=2.12.11
ARG SBT_VERSION=1.7.2


#RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
#  apk add --no-cache bash && \
#  cd "/tmp" && \
#  wget "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
#  tar xzf "scala-${SCALA_VERSION}.tgz" && \
#  mkdir "${SCALA_HOME}" && \
#  rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
#  mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
#  ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
#  apk del .build-dependencies && \
#  rm -rf "/tmp/"* \

# Update Alpine Linux Package Manager and Install the bash
RUN apk update && apk add bash

# Install sbt
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

CMD ["sbt", "run", "-Dsbt.rootdir=true"]