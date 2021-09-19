# Chapter 12 Exercises.
The chapter exercises will take place in Apache Zeppelin and across two streaming applications. The data produced across the streaming applications can also be observed in the Apache Zeppelin session, enabling you to dynamically work with the streaming data interactively.

# Running the Environment
Follow the directions located at `ch-12/docker/README.md` to get your local environment up and running. There is an opportunity to create a new `zeppelin-spark` container to power the chapter materials as well. 

## Exercise 1: Common DataFrame Functions
This first exercise will introduce you to the common methods and functional transformations available to you with a special interest on using functions that will provide you a leg up when dealing with analytical data and producing insights. You'll find the final working notebook under the `exercises/01_common_dataframe_functions` directory. You can import this directly into Zeppelin or follow along in the book to build up the entire notebook yourself.

## Exercise 2: Grouping and Aggregations
Using the new skills gained in exercise 1, you'll take the `coffee_orders` data from `Chapter 11` and add `derived` columns to help assist you while you create a manual `rollup` job across `year/month/store_id`.