var express = require('express');
var https = require('https');

var app = express();
app.use(express.bodyParser());

var logger = require('./logger');
require('./routes')(app, logger);

var security = require('./security');
var httpsServer = https.createServer(security.credentials, app);
httpsServer.listen(6666);