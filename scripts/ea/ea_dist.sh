#!/bin/sh

CODEBRAG_ROOT=../..
DIST_DIR=codebrag
DIST_ARCHIVE=codebrag.zip
WAR_FILE=$CODEBRAG_ROOT/codebrag-dist/target/scala-2.10/codebrag-dist-assembly*.jar
GUIDE_FILE=$CODEBRAG_ROOT/scripts/ea/ea_install_guide.pdf
CONF_FILE=$CODEBRAG_ROOT/codebrag-rest/src/main/resources/application.conf.template
LOG_CONF=$CODEBRAG_ROOT/scripts/logback-example.xml
RUN_SCRIPT=$CODEBRAG_ROOT/scripts/ea/run.sh


echo "Preparing EA distribution of Codebrag"
echo "Cleanup"
rm -rf $DIST_DIR
rm -rf $DIST_ARCHIVE
mkdir $DIST_DIR

echo "Copying application"
cp $WAR_FILE $DIST_DIR/codebrag.jar
echo "Done"

echo "Copying run script"
cp $RUN_SCRIPT $DIST_DIR
echo "Done"

echo "Copying instruction"
cp $GUIDE_FILE $DIST_DIR
echo "Done"

echo "Copying codebrag configuration file"
cp $CONF_FILE $DIST_DIR/codebrag.conf
echo "Done"

echo "Copying logging configuration file"
cp $LOG_CONF $DIST_DIR/logback.xml
echo "Done"

echo "Packaging"
zip -r $DIST_ARCHIVE $DIST_DIR
echo "Done"

echo "All done!"
