# Compiling Protobuf with ScalaPB

Using the standalone compiler which is located at [https://scalapb.github.io/docs/scalapbc/](https://scalapb.github.io/docs/scalapbc/)

1. Download the compiler to a directory of your choice (I used ~/install/)
2. Export the SCALAPBC location.
   ~~~
   export SCALAPBC=~/install/scalapbc/scalapbc-0.11.1
   ~~~
3. Export the path to the `spark-moderndataengineering` directory.
   `Note` This location will be where you downloaded the hands-on exercise material to. My example is shown below.
   ~~~
   export SPARK_MDE_HOME=/Users/`whoami`/git/newfront/spark-moderndataengineering
   ~~~
4. Follow the directions to compile the scalapb below

## Compiling the protobuf
This process will generate the scala classes that you can use in your code base, directly from the `coffee.common.proto` definition.

~~~
export APPLICATION_DIR=$SPARK_MDE_HOME/ch-11/applications/spark-kafka-coffee-orders
export PROTO_HOME=$APPLICATION_DIR/data/protobuf

$SCALAPBC/bin/scalapbc -v3.11.1 \
  --proto_path=$PROTO_HOME \
  --scala_out=$APPLICATION_DIR/src/main/scala \
  coffee.common.proto
~~~

## SparkSQL with ScalaPB
https://scalapb.github.io/docs/sparksql/