#!/bin/bash

PWD=${PWD}
DOCKER_NETWORK_NAME='mde'
DOCKER_COMPOSE_FILE='docker-compose-all.yaml'

function sparkConf() {
  echo "applying spark.conf"
}

function start() {
  docker network create -d bridge ${DOCKER_NETWORK_NAME}
  docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --remove-orphans zeppelin
}

function stop() {
  docker-compose -f ${DOCKER_COMPOSE_FILE} down zeppelin
}

case "$1" in
  start)
    start
  ;;
  stop)
    stop
  ;;
  *)
    echo $"Usage: $0 {start | stop}"
  ;;
esac

