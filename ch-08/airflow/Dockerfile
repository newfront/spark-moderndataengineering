ARG AIRFLOW_BASE_IMAGE=apache/airflow:2.1.0

FROM docker.io/${AIRFLOW_BASE_IMAGE}

ARG JAVA_LIBRARY=openjdk-11-jdk-headless
ENV JAVA_LIBRARY=${JAVA_LIBRARY}

USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ${JAVA_LIBRARY} \
    && apt-get autoremove -yqq --purge \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

USER airflow
RUN pip install --no-cache-dir --user apache-airflow-providers-apache-spark
