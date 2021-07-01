# Compiling Protobuf with ScalaPB

Using the standalone compiler which is located at [https://scalapb.github.io/docs/scalapbc/](https://scalapb.github.io/docs/scalapbc/)

1. Download the compiler to a directory of your choice (I used ~/install/)
2. Export the SCALAPBC location.
   ~~~
   export SCALAPBC=~/install/scalapbc/scalapbc-0.11.1
   ~~~
3. Export the path to the `spark-moderndataengineering` directory.
   ~~~
   export SPARK_MDE_HOME=/Users/`whoami`/git/newfront/spark-moderndataengineering
   ~~~
4. Follow the directions to compile the scalapb below

## Compiling the protobuf
This process will generate the scala classes that you can use in your code base, directly from the `coffee.proto` definition.

~~~
mkdir /Users/`whoami`/Desktop/coffee_protos
$SCALAPBC/bin/scalapbc -v3.11.1 \
  --scala_out=/Users/`whoami`/Desktop/coffee_protos \
  --proto_path=$SPARK_MDE_HOME/ch-09/data/protobuf/ \
  coffee.proto
~~~

## SparkSQL with ScalaPB
https://scalapb.github.io/docs/sparksql/

