echo "Starting Codebrag..."
java -Dfile.encoding=UTF-8 -Dconfig.file=./codebrag.conf -Dlogback.configurationFile=./logback.xml -jar codebrag.jar
echo "Codebrag started. Logs are written to codebrag.log"