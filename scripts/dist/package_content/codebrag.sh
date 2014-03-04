#!/bin/bash
# For debugging purposes uncomment next line
#set -x

if [ -z $CODEBRAG_HOME ]; then
  CURRENT_DIR=$(pwd)
  if [ ! -f "$CURRENT_DIR/codebrag.jar" ]; then
    echo "ERROR: You need to either have CODEBRAG_HOME env variable set or run Codebrag from its directory"
    exit 1    
  else
    CODEBRAG_HOME=$CURRENT_DIR
  fi
fi

APP_NAME="Codebrag"
APP_FILENAME="codebrag"
APP_PATH=$CODEBRAG_HOME
APP_PID="$APP_PATH/$APP_FILENAME.pid"
APP_FILE=$APP_PATH/$APP_FILENAME".jar"
APP_CONFIG_FILE="$CODEBRAG_HOME/$APP_FILENAME.conf"
APP_LOG_CONFIG_FILE="$CODEBRAG_HOME/logback.xml"
APP_COMMAND="java -Dfile.encoding=UTF-8 -Dconfig.file=$APP_CONFIG_FILE -Dlogback.configurationFile=$APP_LOG_CONFIG_FILE -jar $APP_FILE"

start() {
  checkpid
	STATUS=$?
	if [ $STATUS -ne 0 ]; then
		echo "Starting $APP_NAME in $APP_PATH..."
    cd $CODEBRAG_HOME
    ./preconditions.sh
    CHECK_STATUS=$?
    if [ $CHECK_STATUS -ne 0 ]; then
      echo "Exiting..."
      exit 1
    fi
		nohup $APP_COMMAND > /dev/null 2>&1 &
		echo PID $!
		echo $! > $APP_PID
		statusit
  else
  	echo "$APP_NAME Already Running"
  fi
}

stop() {
  checkpid
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		echo "Stopping $APP_NAME..."
		kill `cat $APP_PID`
		rm $APP_PID
		statusit
	else
		echo "$APP_NAME - Already killed"
	fi
}

checkpid(){
  # Should Not Be altered
  local TMP_FILE="/tmp/status_$APP_FILENAME"
  local STATUS=9
  if [ -f $APP_PID ]; then
		#echo "Is Running if you can see next line with $APP_NAME"
		ps -fp `cat $APP_PID` | grep $APP_FILE > $TMP_FILE
		if [ -f $TMP_FILE -a -s $TMP_FILE ] ;
			then
				STATUS=0
				#"Is Running (PID `cat $APP_PID`)"
			else
				STATUS=2
				#"Stopped incorrectly"
			fi

		## Clean after yourself
		rm -f $TMP_FILE
	else
		STATUS=1
		#"Not Running"
	fi

	return $STATUS
}

statusit() {
  ### For internal usage
  local STATUS_CODE[0]="Is Running"
  local STATUS_CODE[1]="Not Running"
  local STATUS_CODE[2]="Stopped incorrectly"
  local STATUS_CODE[9]="Default Status, should not be seen"

  checkpid
	STATUS=$?
	EXITSTATUS=${STATUS_CODE[STATUS]}
	if [ $STATUS -eq 0 ]; then
		EXITSTATUS=${STATUS_CODE[STATUS]}" (PID `cat $APP_PID`)"
	fi
  echo $APP_NAME - $EXITSTATUS
}



case "$1" in

    'start')
        start
        ;;

    'stop')
        stop
        ;;

    'restart')
        stop
        start
        ;;

    'status')
        statusit
        ;;

    *)
        echo "Usage: $0 { start | stop | restart | status }"
        exit 1
        ;;
esac

exit 0