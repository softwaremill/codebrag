#!/bin/sh

java -DwithInMemory=true -Xms512m -Xmx512m -jar sbt-launch.jar container:start "~ compile"
