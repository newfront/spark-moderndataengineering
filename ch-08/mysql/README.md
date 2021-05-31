## This directory should be dropped into ~/dataengineering/ 
# This means you no longer need to worry about moving your mysql data from chapter to chapter

~~~
dataengineering |
  - airflow
  - minio
  - mysql
  - spark
~~~

## Bootstrap Again (or copy your /data/mysqldir from prior experiements)
~~~
cd ~/dataengineering/mysql && ./copy-and-bootstrap.sh
~~~

Now you will have a brand new Hive Metastore