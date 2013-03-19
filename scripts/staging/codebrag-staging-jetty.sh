#!/bin/sh
DOWNLOAD_HOST="http://ftp.man.poznan.pl/eclipse/jetty"
VERSION="8.1.9.v20130131"
#
DIST_NAME="jetty-distribution-"$VERSION
wget $DOWNLOAD_HOST/$VERSION/dist/jetty-distribution-$VERSION.tar.gz
tar xvf $DIST_NAME.tar.gz
rm -f $DIST_NAME.tar.gz
ln -s $DIST_NAME jetty
