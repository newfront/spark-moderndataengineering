#!/bin/bash

bootstrap_sql="${PWD}/bootstrap.sql"
bootstrap_hive="${PWD}/bootstrap-hive.sql"

user=${MYSQL_USER}
password=${MYSQL_PASSWORD}
db_root_pw=${MYSQL_ROOT_PASSWORD}

cat > ${PWD}/secrets <<EOL
[client]
user=${user}
password=${password}
EOL

cat > ${PWD}/secrets_root <<EOL
[client]
user=root
password=${password}
EOL

mysql --defaults-file=${PWD}/secrets < ${bootstrap_sql}

# Trigger the hive-bootstrap (this will run in the K8s pod)
mysql -u root -p < bootstrap-hive.sql
