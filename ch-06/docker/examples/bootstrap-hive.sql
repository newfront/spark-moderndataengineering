CREATE DATABASE `metastore`;

/* Apply User Grants on our dataeng user */
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'dataeng'@'%';
GRANT ALL PRIVILEGES ON `default`.* TO 'dataeng'@'%';
GRANT ALL PRIVILEGES ON `metastore`.* TO 'dataeng'@'%';
FLUSH PRIVILEGES;

USE `metastore`;

SOURCE /hive-schema-2.3.0.mysql.sql
