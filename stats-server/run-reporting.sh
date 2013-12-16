#!/bin/bash

./node_modules/.bin/forever start -o reporting-out.log -e reporting-err.log lib/reporting/reporting_server.js