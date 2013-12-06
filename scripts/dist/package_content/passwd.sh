#!/bin/sh

CONFIG=${1:-"codebrag.conf"}
PASSWD_MAIN=com.softwaremill.codebrag.tools.ChangeUserPassword

java -cp codebrag.jar -Dconfig.file=./$CONFIG $PASSWD_MAIN