#!/bin/sh

CONFIG=${1:-"codebrag.conf"}

echo "Starting Codebrag... (with config:$CONFIG)"
nohup java -Dconfig.file=./$CONFIG -Dlogback.configurationFile=./logback.xml -jar codebrag.jar &
echo "Codebrag started. Logs are written to codebrag.log"