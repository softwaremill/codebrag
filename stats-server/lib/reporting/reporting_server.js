var http = require('http'),
    express = require('express'),
    db = require('../db');

function configureExpressApp() {
  var app = express();
  app.use(genericErrorHandler);
  console.log(__dirname + '../../public');
  app.use(express.static(__dirname + '/../../public'));
  return app;

  function genericErrorHandler(err, req, res, next) {
    res.status(500);
    res.json({ error: "Something went wrong with reports generation" });
  }
}

function initializeRoutes(app, db) {
  require('./reports_routes')(app, db);
}

function startServer(app) {
  var port = 4000;
  var httpServer = http.createServer(app);
  httpServer.listen(port);
  console.log("Codebrag reporting server started on port", port);
  return httpServer;
}


db.initialize().then(function(db) {
  var app = configureExpressApp();
  initializeRoutes(app, db);
  startServer(app);
}).done();


