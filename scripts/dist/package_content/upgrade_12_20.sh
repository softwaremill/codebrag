#!/bin/bash

echo "Upgrading Codebrag 1.2 to 2.0"
CONFIG=${1:-"codebrag.conf"}

java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.dao.sql.MigrateV1_2ToV2_0
exit 0