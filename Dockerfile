#This is just a sample Docker build file for the application. Feel free to make changes in order to suit your needs.

FROM openjdk:17-alpine

RUN mkdir -p /root/zio-microservice/

# Update Alpine Linux Package Manager and Install the bash
RUN apk update && apk add bash

ADD dist/zio-microservice-seed.jar /root/zio-microservice/zio-microservice-seed.jar


WORKDIR /root/zio-microservice

RUN echo '#!/bin/ash' >> /root/zio-microservice/run.sh
RUN echo 'java -jar /root/zio-microservice/zio-microservice-seed.jar' >> /root/zio-microservice/run.sh
RUN chmod a+x /root/zio-microservice/run.sh

EXPOSE 8080

CMD [ "/root/zio-microservice/run.sh" ]