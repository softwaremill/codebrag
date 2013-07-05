# Script for restoring database state from a dump file on TechCrunch demo server
#!/bin/bash
mv ~/codebrag-dump.tar.gz ~/mongo/bin
cd ~/mongo/bin
tar xvf codebrag-dump.tar.gz
./mongo --eval "codebrag.dropDatabase()"
./mongorestore dump