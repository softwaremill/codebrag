#!/bin/bash

./node_modules/.bin/forever start -o stats-out.log -e stats-err.log lib/server.js