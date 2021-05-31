
# Installs and bootstraps Hive for a fresh start
docker cp "${PWD}/install/bootstrap.sh" "mysql:/"
docker cp "${PWD}/install/bootstrap.sql" "mysql:/"
docker cp "${PWD}/install/bootstrap-hive.sql" "mysql:/"
docker cp "${PWD}/install/hive-schema-2.3.0.mysql.sql" "mysql:/"
docker cp "${PWD}/install/hive-txn-schema-2.3.0.mysql.sql" "mysql:/"

# setup some initial stuff
docker exec mysql /bootstrap.sh

# bootstrap hive (requires dataengineering password)
echo "Installing Hive Tables. You will be prompted for the MySQL Root Password. (dat**nginer***)"
docker exec --user root -it mysql bash -c "mysql -u root -p < bootstrap-hive.sql"
