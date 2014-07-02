#!/bin/bash

echo "Upgrading Codebrag 2.1 to 2.2"
CONFIG=${1:-"codebrag.conf"}

java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_1ToV2_2
exit 0