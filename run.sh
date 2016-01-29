#!/bin/sh

cd codebrag-ui
nohup npm install && ./node_modules/.bin/grunt server &

cd ../
java -Dfile.encoding=UTF8 -Xmx1590M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024m -jar sbt-launch.jar "~ container:start"
