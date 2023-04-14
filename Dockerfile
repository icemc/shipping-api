#This is just a sample Docker build file for the application. Feel free to make changes in order to suit your needs.
FROM openjdk:alpine

MAINTAINER dev@hiis.io

RUN mkdir -p /root/zio-microservice-seed/

ADD modules/application/target/scala-2.13/zio-microservice-seed.jar /root/zio-microservice-seed/zio-microservice-seed.jar


WORKDIR /root/zio-microservice-seed

RUN echo '#!/bin/ash' >> /root/zio-microservice-seed/run.sh
RUN echo 'java -jar /root/zio-microservice-seed/zio-microservice-seed.jar' >> /root/zio-microservice-seed/run.sh
RUN chmod a+x /root/zio-microservice-seed/run.sh

CMD [ "/root/zio-microservice-seed/run.sh" ]