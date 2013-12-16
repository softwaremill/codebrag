#!/bin/bash

./node_modules/.bin/forever start -o logs/stats-out.log -e logs/stats-err.log lib/server.js