# Exercise 2. Generating Binary Serializable Event Data with Spark and Publishing to Kafka
During this exercise you'll learn how to work with binary serialized structured data inside of Spark. This is an essential skill for Data Engineers focused on interoperability with systems running on outside of the data platform ecosystem. Typically data is encoded as binary avro or protobuf and then emitted through an API gateway into one or more Kafka topics. This enables downstream consumers to fetch the raw data as it enters the data network, once initial validation has been completed (like we covered in Chapter 9).


