# Chapter 12: Towards Advanced Analytical Processing & Insights
This chapter marks the final stretch of your journey across the many pillars supporting modern data engineering. This chapter will be the last tactical chapter teaching new functionality before moving onto deployment patterns and monitoring. 

## Chapter Exercises and Materials Overview
This chapter is act as a thorough introduction to using Apache Spark's analytical functionality. You will be taking a hybrid approach while working with the content in this exercise. The first section will be utilizing the dynamic environment provided by `Apache Zeppelin` as you learn to use the standard core `SparkSQL` functions like `window`, `groupBy`, `pivot`, `cube`, and `rollup`. You will also be looking at using the `agg` method on a grouped `DataFrame` to create a batch based aggregation, and lastly will look at registering and using User Defined Functions aka `udfs`.

Afterwards, you'll be looking at Streaming analytics and insight patterns. This content will take up the rest of the chapter as you explore how to use simple stateful DataFrame aggregations across timeseries windows. The windowing functionality is a great opportunity to also talk about time-based `Watermarking`. The watermarking process informs the Spark engine when it is appropriate to ignore late arriving data, which is controlled through the `withWatermark` functionality on the Spark DAG.

Lastly, you'll be taking this whole process to the next level and learning what to do when the problems you are trying to tackle don't fall into the standard methods provided by Apache Spark. This is where the advanced `flatMapGroupsWithState` method on the typed Dataset comes into play. This method allows you to dictate how `Spark` handles stateful aggregations as this can be thought of as a UDF on steroids.

