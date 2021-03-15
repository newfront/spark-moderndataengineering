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
  if [ ! -d "${PWD}/data/mysqldir" -d ]; then
    echo "mysqldir doesn't exist. Adding docker/data/mysqldir for mysql database"
    mkdir "${PWD}/data/mysqldir"
  fi
  sparkExists
  createNetwork
  docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --remove-orphans mysql
  docker-compose -f ${DOCKER_COMPOSE_FILE} up -d --remove-orphans zeppelin
  echo "Zeppelin will be running on http://127.0.0.1:8080"
}

function stop() {
  docker-compose -f ${DOCKER_COMPOSE_FILE} down --remove-orphans
}

function restart() {
  stop && start
}
function bootstrap() {
  docker cp "${PWD}/examples/bootstrap.sh" "mysql:/"
  docker cp "${PWD}/examples/bootstrap.sql" "mysql:/"
  hiveInit
  docker exec mysql /bootstrap.sh

  echo "To bootstrap hive. You have to do so as root from within the docker context"
  echo "1. docker exec -it mysql bash"
  echo "2. mysql -u root -p"
  echo "3. source bootstrap-hive.sql"
  echo "See http://localhost:8080/#/notebook/2FZZXJZCP for more details."
}

function hiveInit() {
  # copy the hive init db files for bootstrap
  docker cp "${PWD}/examples/bootstrap-hive.sql" "mysql:/"
  docker cp "${PWD}/hive/install/hive-schema-2.3.0.mysql.sql" "mysql:/"
  docker cp "${PWD}/hive/install/hive-txn-schema-2.3.0.mysql.sql" "mysql:/"
}

case "$1" in
  start)
    start
  ;;
  stop)
    stop
  ;;
  restart)
    restart
  ;;
  hiveInit)
    hiveInit
  ;;
  bootstrap)
    # setup the mysql database and tables
    bootstrap
  ;;
  *)
    echo $"Usage: $0 {start | stop | restart | initHive | bootstrap }"
  ;;
esac
