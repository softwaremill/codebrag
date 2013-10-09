#!/bin/bash

CONFIG=${1:-"codebrag.conf"}

kill $(ps aux | grep ".*java.*$CONFIG.*codebrag.jar" | grep -v grep | awk '{print $2}')
exit 0