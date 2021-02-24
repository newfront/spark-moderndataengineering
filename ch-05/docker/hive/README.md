# Bootstrap The Hive Metastore

1. Copy the `hive-schema-2.3.0.mysql.sql` into the mysql docker container

~~~
cd /path/to/ch-05/docker && ./run.sh hiveInit
~~~

The hiveInit will run the following command. That will copy the SQL file into MySQL
~~~
docker cp hive/install/hive-schema-2.3.0.mysql.sql mysql:/
docker cp hive/install/hive-txn-schema-2.3.0.mysql.sql mysql:/
~~~

2. Connect to the mysql docker container
~~~
docker exec -it mysql bash
~~~

3.  Authenticate as the Root MySQL User
Follow the command below from within in mysql docker container to authenticate to mysql using `-u root -p` and the `MYSQL_ROOT_PASSWORD`. The MYSQL_ROOT_PASSWORD value can be found in the `docker-compose-all.yaml`
~~~
mysql -u root -p
~~~

4. Create the hive metastore database
~~~
mysql> CREATE DATABASE `metastore`;
~~~

5. Grant the dataeng user access to the metastore db
~~~
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'dataeng'@'%';
GRANT ALL PRIVILEGES ON metastore.* TO 'dataeng'@'%';
FLUSH PRIVILEGES;
~~~

6. Exit as the root user
~~~
mysql> exit
~~~

7. Authenticate into MySQL as the `dataeng` user
~~~
mysql -u dataeng -p
~~~

8. Switch Databases to the `metastore`
~~~
mysql> use metastore;
~~~

9. Use the MYSQL `SOURCE` command to read in the hive 2.3.0 schema
This is the file that you copied using `docker cp` in Step 1
~~~
mysql> SOURCE /hive-schema-2.3.0.mysql.sql;
~~~

Now you will be able to use the Hive Metastore.

10. Check that you have all the Tables
From the mysql commandline `mysql>`. Run the following.
~~~
use metastore;
show tables;
~~~

You should see
~~~
+---------------------------+
| Tables_in_metastore       |
+---------------------------+
| AUX_TABLE                 |
| BUCKETING_COLS            |
| CDS                       |
| COLUMNS_V2                |
| COMPACTION_QUEUE          |
| COMPLETED_COMPACTIONS     |
| COMPLETED_TXN_COMPONENTS  |
| DATABASE_PARAMS           |
| DB_PRIVS                  |
| DBS                       |
| DELEGATION_TOKENS         |
| FUNC_RU                   |
| FUNCS                     |
| GLOBAL_PRIVS              |
| HIVE_LOCKS                |
| IDXS                      |
| INDEX_PARAMS              |
| KEY_CONSTRAINTS           |
| MASTER_KEYS               |
| NEXT_COMPACTION_QUEUE_ID  |
| NEXT_LOCK_ID              |
| NEXT_TXN_ID               |
| NOTIFICATION_LOG          |
| NOTIFICATION_SEQUENCE     |
| NUCLEUS_TABLES            |
| PART_COL_PRIVS            |
| PART_COL_STATS            |
| PART_PRIVS                |
| PARTITION_EVENTS          |
| PARTITION_KEY_VALS        |
| PARTITION_KEYS            |
| PARTITION_PARAMS          |
| PARTITIONS                |
| ROLE_MAP                  |
| ROLES                     |
| SD_PARAMS                 |
| SDS                       |
| SEQUENCE_TABLE            |
| SERDE_PARAMS              |
| SERDES                    |
| SKEWED_COL_NAMES          |
| SKEWED_COL_VALUE_LOC_MAP  |
| SKEWED_STRING_LIST        |
| SKEWED_STRING_LIST_VALUES |
| SKEWED_VALUES             |
| SORT_COLS                 |
| TAB_COL_STATS             |
| TABLE_PARAMS              |
| TBL_COL_PRIVS             |
| TBL_PRIVS                 |
| TBLS                      |
| TXN_COMPONENTS            |
| TXNS                      |
| TYPE_FIELDS               |
| TYPES                     |
| VERSION                   |
| WRITE_SET                 |
+---------------------------+
57 rows in set (0.00 sec)
~~~

Now you are golden.
