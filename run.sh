#!/bin/sh

java -Xms512m -Xmx1024m -jar sbt-launch.jar container:start "~ compile"
