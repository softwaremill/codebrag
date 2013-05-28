#!/bin/sh

java -Xms1024m -Xmx1024m -jar sbt-launch.jar container:start "~ compile"
