"""
Simple First Spark DAG: 
Uses the `SparkSubmitOperator` with the Airflow Variable `SparkHome` to run the `SparkPi` example.
"""

from airflow.models import DAG, Variable
spark_home = Variable.get("SPARK_HOME")

from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.utils.dates import days_ago

args = {
    'owner': 'airflow',
}

with DAG(
    dag_id='daily_spark_report',
    default_args=args,
    schedule_interval=None,
    start_date=days_ago(2),
    tags=['coffeeco'],
) as dag:
    submit_job = SparkSubmitOperator(
        application=f'{spark_home}/examples/jars/spark-examples_2.12-3.1.1.jar',
        conn_id="local_spark_connection",
        java_class="org.apache.spark.examples.SparkPi",
        task_id="submit_job"
    )
