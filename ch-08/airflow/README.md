# Setting Up Airflow
These directions are provided from the [Official Airflow Docker Documentation](https://airflow.apache.org/docs/apache-airflow/stable/start/docker.html)

## Add the required local directories
These directories will be volume mounted into the Airflow Docker runtime
~~~
mkdir ./dags ./logs ./plugins
~~~

### Add the Environment File
This will enable the local host runtime and the container runtime to work with the same user. *This is needed for Linux or Linux-style environments - which includes Mac*

~~~
echo -e "AIRFLOW_UID=$(id -u)\nAIRFLOW_GID=0\n_AIRFLOW_WEB_SERVER_PORT=8088" > .env
~~~

## Start up the Shared Bridged Network
~~~
docker network create -d bridge mde
~~~

## Initial Setup Work
You must run this `once` before you can get started. This is the initial bootstrap process. This process will download all of the required Docker container images, and run the initialization sequence required to run Airflow.

~~~
docker-compose up airflow-init
~~~

You will see a bunch of debug logging during this process. You can scroll through this to see what the initalization process is doing. Ultimately, this process is in charge of running the database setup work and migrations, bootstrapping and all initalization scripts. Essentially, everything you need to get up and running on Apache Airflow.

## Running Basic Airflow
~~~
docker-compose up
~~~

# Advanced Operation

## Building Custom Image with the Spark Airflow Provider
To use `Apache Spark` along with the `Apache Airflow` containers, you need to install the `apache-airflow-providers-apache-spark` plugin. This can be done with the core docker container `apache/airflow` but given there is a cluster of machines, it is easier to install by extending the base Airflow container.

**Build the Local Airflow Environment with Spark Providers and Java11**
~~~
docker build . \
  --no-cache=true \
  --build-arg AIRFLOW_BASE_IMAGE="apache/airflow:2.1.0" \
  --build-arg JAVA_LIBRARY="openjdk-11-jdk-headless" \
  --tag `whoami`/apache-airflow-spark:2.1.0
~~~

_Why add Java? You will need to be running a version of Java that is built to work in the Linux environment_. Chances are your local Java doesn't play nice with Linux due to Operating System differences between MacOS/Windows and Linux.

## Using The newfrontdocker Image
[DockerHub Repo](https://hub.docker.com/repository/docker/newfrontdocker/apache-airflow-spark)

Update your `.env` to include the

~~~
echo -e "AIRFLOW_IMAGE_NAME=newfrontdocker/apache-airflow-spark:2.1.0" >> .env
echo -e "SPARK_HOME=${SPARK_HOME}" >> .env
echo -e "JAVA_HOME=${JAVA_HOME}" >> .env
~~~


***
Note: (MacOS - BigSur)
If you see `error creating mount path error while creating mount source path` for Java. You might be experiencing an issue with gRPC Fuse. 
1. Open up the `Docker Desktop` app. 
2. Click on the Config Gear (Preferences) 
3. Uncheck `Use gRPC Fuse`. This will restart `docker` and return to the original osxfs file sharing.
Reference (https://github.com/docker/for-mac/issues/4999)

If you see an issue related to mounting java. `Error response from daemon: invalid mount config for type "bind": bind source path does not exist: /Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home/` then you can just make a non-symlink copy in the directory.

~~~
cp -R $JAVA_HOME ./java11
echo -e "JAVA_HOME=${JAVA_HOME}" >> .env
~~~

Now you should now be up and running.

***

## Skip the Docker Build, installing the provider on the webserver, workers

1. Use the `--user airflow` parameter to log into the airflow webserver
~~~
docker exec --user airflow -it airflow_airflow-webserver_1 bash
~~~

2. Install the `apache-airflow-providers-apache-spark` plugin
~~~
pip install --no-cache --user apache-airflow-providers-apache-spark
~~~

This is everything you need to get started. Head on over to http://127.0.0.1:8088 to login.

### Official Docker Image Docs
https://airflow.apache.org/docs/apache-airflow/stable/start/docker.html
