#!/bin/bash

function java_not_installed {
  local JAVA_VERSION_REQUIRED=7
  local JAVA_VERSION_CURRENT=`java -version 2>&1 | grep "java version" | awk '{print $3}' | tr -d \" | awk '{split($0,numbers,"."); print numbers[2]}'`
  if [ $JAVA_VERSION_CURRENT -lt $JAVA_VERSION_REQUIRED ]; then
    echo "ERROR: Java JDK in required version $JAVA_VERSION_REQUIRED not found" >&2
    echo 0
  else
    echo 1
  fi
}

if [[ $(java_not_installed) -eq 0 ]]; then
  echo "Please install required version of Java first"
  exit 1
fi

CONFIG=${1:-"codebrag.conf"}

echo "Starting Codebrag... (with config:$CONFIG)"
nohup java -Dfile.encoding=UTF-8 -Dconfig.file=./$CONFIG -Dlogback.configurationFile=./logback.xml -jar codebrag.jar &
echo "Codebrag started. Logs are written to codebrag.log"


