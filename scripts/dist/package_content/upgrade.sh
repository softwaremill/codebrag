#!/bin/bash

echo "Upgrading Codebrag 1.0 to 1.2"
CONFIG=${1:-"codebrag.conf"}

java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.dao.sql.MigrateMongoToSQL
exit 0