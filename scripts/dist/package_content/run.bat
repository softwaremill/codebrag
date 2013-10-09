echo "Starting Codebrag..."
java -Dconfig.file=./codebrag.conf -Dlogback.configurationFile=./logback.xml -jar codebrag.jar
echo "Codebrag started. Logs are written to codebrag.log"