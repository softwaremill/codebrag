#!/bin/bash

./node_modules/.bin/forever start -o out.log -e err.log lib/server.js