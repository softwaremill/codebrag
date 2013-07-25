#!/bin/sh

echo "Starting Codebrag..."
nohup java -Dconfig.file=./codebrag.conf -Dlogback.configurationFile=./logback.xml -jar codebrag.jar &
echo "Codebrag started"