# Chapter 10
## Writing Applications with Apache Spark Structured Streaming Structured Data Models
- Binary Serializable Events

## Installing SBT
SBT is a scala build tool. You can download it from [scala-sbt.org](https://www.scala-sbt.org/)

Installing `sbt` can be done easily with Homebrew if you have it installed: `brew install sbt`

## Compiling the Protobuf Events 
This build can be further customize the sbt build. [Here](https://scalapb.github.io/docs/sbt-settings/) is a full list of sbt options for scalapb
~~~
sbt clean compile
~~~

## Running the Unit Tests
~~~
sbt test
~~~

## Creating the assembly
~~~
sbt assembly
~~~

### Notes:
Tested with Java 11 and Scala 2.12

