# Using Minio as an S3 Clone
MinIO can be used as a drop in replacement for Amazon S3 (this is good for local testing and can also be used to run your own Object Store)

[MinIO DockerHub](https://hub.docker.com/r/minio/minio)

## Export the Common Location (or add to .zshrc/.bash_profile)
~~~bash
export DATA_ENGINEERING_BASEDIR="~/dataengineering"
~~~

## Setup the environment manually
~~~bash
mkdir ${DATA_ENGINEERING_BASEDIR}/minio && cd ${DATA_ENGINEERING_BASEDIR}/minio/
~~~

or

~~~
cp -R /path/to/ch-08/minio ~/dataengineering
~~~

### Add the initial bucket
~~~
mkdir ${PWD}/data
mkdir ${PWD}/data/com.coffeeco.data/
mkdir ${PWD}/data/com.coffeeco.data/warehouse/
~~~

## Add the aliases to your profile (.zshrc/.bash_profile)
~~~bash
alias minio_start="docker compose -f ${DATA_ENGINEERING_BASEDIR}/minio/docker-compose.yaml up -d"
alias minio_stop="docker compose -f ${DATA_ENGINEERING_BASEDIR}/minio/docker-compose.yaml down --remove-orphans"
~~~

Activate your MinIO aliases using `source ~/.zshrc` or the equivalent. 

# Run MinIO
Ensure you have created the external network `mde` so that you can use MinIO along with your local Spark / Zeppelin / Airflow environments. This will act as your distributed data warehouse.

## Starting MinIO
~~~
minio_start
~~~

## Stopping MinIO
~~~
minio_stop
~~~