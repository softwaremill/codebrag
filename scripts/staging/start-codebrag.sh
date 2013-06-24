#!/bin/bash
#This command is required to redirect port 80 to 8080
sudo /sbin/iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 8080
cd /home/ubuntu/codebrag/
nohup java -Dconfig.file="/home/ubuntu/codebrag/application.conf" -jar codebrag-dist-assembly-0.0.1-SNAPSHOT.jar &