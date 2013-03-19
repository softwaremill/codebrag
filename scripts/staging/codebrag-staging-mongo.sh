#!/bin/bash
MONGO_VERSION=mongodb-linux-x86_64-2.2.3
#
cd ~
wget http://fastdl.mongodb.org/linux/$MONGO_VERSION.tgz
tar xvf $MONGO_VERSION.tgz
rm -f $MONGO_VERSION.tgz
ln -s $MONGO_VERSION mongo
mkdir mongo/data