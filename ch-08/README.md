# Chapter 8: Workflow Orchestration with Apache Airflow
This chapter will teach you the ins and outs of working with Apache Airflow so you can take control of your data pipelines and focus on writing solid software vs juggling the ever evolving requirements of bestowed upon the data engineer.

## Airflow
The `airflow/` directory contains the docker-compose.yaml required to start up airflow and all of its components. This is where you will be working during this chapter. Head into the `airflow` directory and read the README there to initialize airflow and get up and running.

### Volume Mounting Spark
The `docker-compose.yaml` includes a declaration that requires the `$SPARK_HOME` environment variable to be set. If you have followed the prior chapter setups, then this should be available to you.

~~~
echo $SPARK_HOME
~~~

## Optimizing your Local Environment
As you add more and more various technologies to your local data engineering environment, you may find it useful to create a single location (parent directory) that can be used to create an organized local data platform. This can be used to organize the various volume mounts as well as the docker-compose.yaml files that direct docker to run a given technology.

For example, if you want to copy all the hardwork you’ve done so far in this chapter, into a new directory called dataengineering. Then we can easily just copy everything over.

~~~
mkdir ~/dataengineering && cp -R /path/to/ch-08/airflow ~/dataengineering
~~~

The next step is to add a simple alias to start and stop Airflow that can be used as a command from your terminal. You can add the following into your local environment settings (.bashprofile, .bashrc or .zshrc, etc). 

~~~
export DATA_ENGINEERING_BASEDIR="~/dataengineering"

alias airflow2_start="docker compose -f ${DATA_ENGINEERING_BASEDIR}/airflow/docker-compose.yaml up -d"
alias airflow2_stop="docker compose -f ${DATA_ENGINEERING_BASEDIR}/airflow/docker-compose.yaml down --remove-orphans"
~~~

Now whenever you want to spin up Airflow, you can simply type `airflow2_start`, and likewise when you are done for the day or want to gain system resources back you can run `airflow2_stop`. 
