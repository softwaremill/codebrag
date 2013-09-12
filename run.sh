#!/bin/sh

cd codebrag-ui
nohup npm install && grunt server &

cd ../
java -Dfile.encoding=UTF8 -Xmx3000M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024m -jar sbt-launch.jar container:start "~ compile"
