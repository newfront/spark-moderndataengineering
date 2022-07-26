# The Hive Metastore

## Prepare the `mysql` docker container
Copy the `hive-schema-2.3.0.mysql.sql` into the mysql docker container.
~~~
cd /path/to/ch-05/docker && ./run.sh hiveInit
~~~

The `hiveInit` will run the following commands. This copies the hive metastore table generation SQL scripts onto the `mysql` docker.
~~~
docker cp hive/install/hive-schema-2.3.0.mysql.sql mysql:/
docker cp hive/install/hive-txn-schema-2.3.0.mysql.sql mysql:/
~~~

## Connect to the `mysql` docker container
Use the docker exec command to log into the container context.
~~~
docker exec -it mysql bash
~~~

## Authenticate as the `root` MySQL User
Follow the command below from within in mysql docker container to authenticate to mysql using `-u root -p` and the `MYSQL_ROOT_PASSWORD`. The MYSQL_ROOT_PASSWORD value can be found in the `docker-compose-all.yaml`
~~~
mysql -u root -p
~~~

## Create the hive `metastore` database
~~~
mysql> CREATE DATABASE `metastore`;
~~~

## Grant the `dataeng` user access to the `metastore` db

`It is worth noting that you can also create new users who have specific access to only a few tables, or all tables, or only read or write access. This allows you to govern the database access using privileges bound to a users grant permissions.`

~~~
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'dataeng'@'%';
GRANT ALL PRIVILEGES ON `default`.* TO 'dataeng'@'%';
GRANT ALL PRIVILEGES ON `metastore`.* TO 'dataeng'@'%';
FLUSH PRIVILEGES;
~~~

### Exit as the `root` user
~~~
mysql> exit
~~~

### Authenticate as the `dataeng` user
~~~
mysql -u dataeng -p
~~~

### Switch Databases to the `metastore`
~~~
mysql> use metastore;
~~~

### Import the Hive Metastore Tables
Use the MySQL `SOURCE` command to read in the hive 2.3.0 schema
This is the file that you copied using `docker cp`.
~~~
mysql> SOURCE /hive-schema-2.3.0.mysql.sql;
~~~

Now you will be able to use the Hive Metastore.

### Check the Tables
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

Now you are golden. Your Hive metastore is ready for action.
