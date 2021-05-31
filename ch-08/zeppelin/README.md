## Zeppelin StandAlone
This depends on `mysql` running for the hive-metastore
This depends on `minio` running for the SQL warehouse

If you've been following along with Chapter 8, you can run the `airflow/docker-compose-chapter-end.yaml` and then spin this up afterwards.

You can pop this zeppelin environment up using the command:
~~~
docker compose -f docker-compose-minio-hive.yaml up
~~~

## Once things are running, go to the notebook to spin things up.
[Bootstrap the Spark SQL S3 Warehouse](http://localhost:8080/#/notebook/2G6C4EH92)

