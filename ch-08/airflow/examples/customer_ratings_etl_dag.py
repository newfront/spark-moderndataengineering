"""
Running our Spark Batch Job via Airflow
"""

from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.models import DAG, Variable
from airflow.utils.dates import days_ago

args = {
    'owner': 'scotteng',
}

spark_home = Variable.get("SPARK_HOME")

# Spark Conf
customer_ratings_conf = {
    "spark.sql.warehouse.dir": "s3a://com.coffeeco.data/warehouse",
    "spark.hadoop.fs.s3a.impl": "org.apache.hadoop.fs.s3a.S3AFileSystem",
    "spark.hadoop.fs.s3a.endpoint": "http://minio:9000",
    "spark.hadoop.fs.s3a.access.key": "minio",
    "spark.hadoop.fs.s3a.secret.key": "minio_admin",
    "spark.hadoop.fs.s3a.path.style.access": "true",
    "spark.sql.catalogImplementation": "hive",
    "spark.sql.hive.metastore.version": "2.3.7",
    "spark.sql.hive.metastore.jars": "builtin",
    "spark.sql.hive.metastore.sharedPrefixes": "org.mariadb.jdbc,com.mysql.cj.jdbc",
    "spark.sql.hive.metastore.schema.verification": "true",
    "spark.sql.hive.metastore.schema.verification.record.version": "true",
    "spark.sql.parquet.compression.codec": "snappy",
    "spark.sql.parquet.mergeSchema": "false",
    "spark.sql.parquet.filterPushdown": "true",
    "spark.hadoop.parquet.enable.summary-metadata": "false",
    "spark.hadoop.mapreduce.fileoutputcommitter.algorithm.version": "2",
    "spark.sql.hive.javax.jdo.option.ConnectionUserName": "dataeng",
    "spark.event.extractor.source.table": "bronze.customerRatings",
    "spark.event.extractor.destination.table": "silver.customerRatings",
    "spark.event.extractor.save.mode": "ErrorIfExists"
}

app_jars=f'{spark_home}/user_jars/hadoop-aws-3.2.0.jar,{spark_home}/user_jars/hadoop-cloud-storage-3.2.0.jar,{spark_home}/user_jars/mariadb-java-client-2.7.2.jar,{spark_home}/user_jars/mysql-connector-java-8.0.23.jar,{spark_home}/user_jars/aws-java-sdk-bundle-1.11.375.jar'
driver_class_path=f'{spark_home}/user_jars/mariadb-java-client-2.7.2.jar:{spark_home}/user_jars/mysql-connector-java-8.0.23.jar:{spark_home}/user_jars/hadoop-aws-3.2.0.jar:{spark_home}/user_jars/hadoop-cloud-storage-3.2.0.jar:{spark_home}/user_jars/aws-java-sdk-bundle-1.11.375.jar'

with DAG(
    dag_id='customer_ratings_etl_dag',
    default_args=args,
    schedule_interval='@daily',
    start_date=days_ago(1),
    tags=['coffeeco', 'core'],
) as dag:
    customer_ratings_etl_job = SparkSubmitOperator(
        application=f'{spark_home}/user_jars/spark-event-extractor.jar',
        jars=app_jars,
        driver_class_path=driver_class_path,
        conf=customer_ratings_conf,
        conn_id="local_spark_connection",
        name='daily-customer_ratings',
        verbose=True,
        java_class="com.coffeeco.data.SparkEventExtractorApp",
        status_poll_interval='20',
        task_id="customer_ratings_etl_job"
    )
