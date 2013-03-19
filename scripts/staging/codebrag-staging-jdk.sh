#!/bin/bash
JDK_GZ_FILE="jdk-7u17-linux-x64.tar.gz"
JDK_URL="http://download.oracle.com/otn-pub/java/jdk/7u17-b02/"$JDK_GZ_FILE
JDK_DIR="jdk1.7.0_17"

wget --no-cookies --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com" $JDK_URL

chown root newprofile
chgrp root newprofile
chmod 0644 newprofile
mv newprofile /etc/profile

mkdir /usr/local/java

mv $JDK_GZ_FILE /usr/local/java/
cd /usr/local/java
chmod a+x $JDK_GZ_FILE
tar xvzf $JDK_GZ_FILE
rm -f $JDK_GZ_FILE
update-alternatives --install "/usr/bin/javac" "javac" "/usr/local/java/"$JDK_DIR"/bin/javac" 1
update-alternatives --set javac /usr/local/java/$JDK_DIR/bin/javac
. /etc/profile