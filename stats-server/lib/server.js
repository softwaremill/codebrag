var express = require('express');
var https = require('https');
var logger = require('./logger');
var security = require('./security');
var db = require('./db');

var app = configureExpressApp();
initializeMongo(initializeRoutes);
startServer(security, app);


function configureExpressApp() {
  var app = express();
  app.use(express.json());
  app.use(express.urlencoded());
  app.use(genericErrorHandler);
  return app;

  function genericErrorHandler(err, req, res, next) {
    res.status(500);
    res.json({ error: "Please check if your request is correct" });
  }
}

function initializeMongo(onSuccessCallback) {
  db.initialize(function(err, _db) {
    if(err) throw err;  // interrupt when can't connect
    onSuccessCallback(_db);
  });
}

function initializeRoutes(db) {
  require('./routes')(app, logger, db);
}

function startServer(security, app) {
  var port = 3000;
  var httpsServer = https.createServer(security.credentials, app);
  httpsServer.listen(port);
  console.log("Codebrag stats server started on port", port);
  return httpsServer;
}
