# Script for restoring database state from a dump file on TechCrunch demo server
#!/bin/bash
mv ~/techcrunch-codebrag-dump.tar.gz ~/mongo/bin
cd ~/mongo/bin
tar xvf techcrunch-codebrag-dump.tar.gz
./mongo --eval "codebrag.dropDatabase()"
./mongorestore dump