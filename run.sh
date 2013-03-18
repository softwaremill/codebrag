#!/bin/sh

java -Xms512m -Xmx512m -jar sbt-launch.jar container:start "~ compile"