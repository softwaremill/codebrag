#!/bin/sh

if [ $# -eq 1 ] && [ $1 = "--help" ]; then
  echo "Prepares and optionally uploads codebrag distribution to S3"
  echo "Usage:"
  echo "dist.sh - packages codebrag distribution as zip file (codebrag.zip)"
  echo "dist.sh upload - packages and uploads package to S3 storage"
  echo "dist.sh upload-preview - packages and uploads preview package to S3 storage"
  exit
fi

CODEBRAG_ROOT=../..
DIST_DIR=codebrag
DIST_ARCHIVE=codebrag.zip
DIST_ARCHIVE_PREVIEW=codebrag-preview.zip
WAR_FILE=$CODEBRAG_ROOT/codebrag-dist/target/scala-2.10/codebrag-dist-assembly*.jar
CONF_FILE=$CODEBRAG_ROOT/codebrag-rest/src/main/resources/application.conf.template
PACKAGE_CONTENT_DIR=$CODEBRAG_ROOT/scripts/dist/package_content


UPLOAD_ARG="upload"
UPLOAD_PREVIEW_ARG="upload-preview"
S3CMD_PATH="./lib/s3cmd-1.5.0-alpha1/s3cmd"
S3CMD_CONFIG="./lib/s3cfg-codebrag"

# Package distribution
echo "Preparing EA distribution of Codebrag"
echo "Cleanup"
rm -rf $DIST_DIR
rm -rf $DIST_ARCHIVE
mkdir $DIST_DIR

echo "Copying application"
cp $WAR_FILE $DIST_DIR/codebrag.jar
echo "Done"

echo "Copying package_content to distribution"
cp -r $PACKAGE_CONTENT_DIR/* $DIST_DIR
echo "Done"

echo "Copying codebrag configuration file"
cp $CONF_FILE $DIST_DIR/codebrag.conf
echo "Done"

echo "Packaging"
zip -r $DIST_ARCHIVE $DIST_DIR
rm -r $DIST_DIR
echo "Done"

# Upload build to S3 if required
if [ -f $DIST_ARCHIVE ] && [ $# -eq 1 ]; then
  if [ $1 = $UPLOAD_ARG ]; then
    DATE_DAY=`date +"%Y-%m-%d"`

    TARGET_PATH="s3://codebrag-dist/$DATE_DAY/$DIST_ARCHIVE"
    echo "Uploading package to $TARGET_PATH"
    CMD="$S3CMD_PATH -c $S3CMD_CONFIG put $DIST_ARCHIVE $TARGET_PATH"
    `$CMD > s3upload.log`

    LATEST_PATH="s3://codebrag-dist/latest/$DIST_ARCHIVE"
    echo "Uploading package to $LATEST_PATH"
    CMD="$S3CMD_PATH -c $S3CMD_CONFIG put --acl-public $DIST_ARCHIVE $LATEST_PATH"
    `$CMD >> s3upload.log`
  elif [ $1 = $UPLOAD_PREVIEW_ARG ]; then
    PREVIEW_PATH="s3://codebrag-dist/preview/$DIST_ARCHIVE_PREVIEW"
    echo "Uploading package to $PREVIEW_PATH"
    CMD="$S3CMD_PATH -c $S3CMD_CONFIG put --acl-public $DIST_ARCHIVE $PREVIEW_PATH"
    `$CMD >> s3upload.log`
  fi
  echo "Done"
fi

echo "All done!"


