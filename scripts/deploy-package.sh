#!/bin/bash

# Expected directories layout is as follows:
# ~/codebrag - contains everything related to codebrag binary
# ~/codebrag/codebrag - currently installed package of codebrag
# ~/codebrag/prev_package - previous package, backup
# ~/codebrag/config - contains instance specific files that will be copied to installation dir
# Repository data should be placed somewhere in ~/codebrag directory and correctly referenced in config file

# exit on any error
set -e

if [ ! $# -eq  2 ]
then
  echo "usage: deploy-package.sh [package zip file] [installation base dir]"
  exit 1
fi

PACKAGE=$1  # zip package file
BASE_DIR=$2 # installation base dir

INSTALL_DIR="$BASE_DIR/codebrag"
BACKUP_DIR="$BASE_DIR/prev_package"
CONFIG_DIR="$BASE_DIR/config"

# backup current version
if [ -d $INSTALL_DIR ]
then
  echo "Backup current version to $BACKUP_DIR"
  rm -rf $BACKUP_DIR
  mv $INSTALL_DIR $BACKUP_DIR
fi

# extract package
echo "Extract new package to $BASE_DIR"
unzip $PACKAGE -d $BASE_DIR

# override package config with instance specific
if [ -d $CONFIG_DIR ]
then
  echo "Overwrite configs with $CONFIG_DIR"
  cp -rf $CONFIG_DIR/* $INSTALL_DIR
fi

# cleanup downloaded package
echo "Cleanup package"
rm $PACKAGE

echo "Done"