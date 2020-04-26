FROM node:alpine3.11

COPY . /www

WORKDIR /www
RUN npm install -g cordova ionic
RUN npm install -D -E @angular/cli
RUN npm install
EXPOSE 8100

RUN ionic serve 8100 --address 0.0.0.0