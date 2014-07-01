#!/bin/bash

echo "Upgrading Codebrag 2.0 to 2.1"
CONFIG=${1:-"codebrag.conf"}

java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_1ToV2_2
exit 0