# Chapter 13 Exercises
The chapter exercises all take place using the application at `/ch-13/applications/stateful-stream-aggs`. You will be learning to use `stateful structured streaming` with `stateful aggregations`. This is a continuation from last chapter (12) where you learned to aggregate data interactively.

## Running the environment for the exercises
The environment that you will be using to run the `stateful-stream-aggs` application will reuse `minio as (s3a)` and `mysql` acting as your `hive-metastore`. Additionally, you will need to run `redis` in order to test the full end to end application.

The `docker-compose-exercise-env.yaml` will get you up and running.

