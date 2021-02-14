#!/bin/bash

PWD=${PWD}
DOCKER_NETWORK_NAME='mde'
DOCKER_COMPOSE_FILE='docker-compose-all.yaml'

function sparkConf() {
  echo "applying spark.conf"
}

function sparkExists() {
  if [ -z "$SPARK_HOME" ]; then
    echo "Missing SPARK_HOME environment variable."
    echo "1. Ensure you have followed the installation instructions for Spark from Chapter 2."
    echo "2. Add SPARK_HOME to your .zshrc or .bashrc, then use `source ~/.zshrc` for example to refresh your session"
    exit 1
  else
    echo "SPARK_HOME is set. location=${SPARK_HOME}"
  fi
}

function createNetwork() {
  cmd="docker network ls | grep ${DOCKER_NETWORK_NAME}"
  eval $cmd
  retVal=$?
  if [ $retVal -ne 0 ]; then
    docker network create -d bridge ${DOCKER_NETWORK_NAME}
  else
    echo "docker network already exists ${DOCKER_NETWORK_NAME}"
  fi
}

function start() {
  # there is an expectation that $SPARK_HOME and $JAVA_HOME are both available to the session
  sparkExists
  createNetwork
  docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --remove-orphans zeppelin
  echo "Zeppelin will be running on http://127.0.0.1:8080"
}

function stop() {
  docker-compose -f ${DOCKER_COMPOSE_FILE} down --remove-orphans
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
