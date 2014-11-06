#!/bin/bash

echo "Upgrading Codebrag 2.2 to 2.3"
CONFIG=${1:-"codebrag.conf"}

java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.migration.MigrateV2_2ToV2_3
java -Dconfig.file=$CONFIG -cp codebrag.jar com.softwaremill.codebrag.migration.FixZombieCommits
exit 0