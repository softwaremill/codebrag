var moment = require('moment');

var MIDDLEWARES = {

  validateRequestBody: function(req, res, next) {
    var json = req.body;
    var valid = json.instanceId && json.instanceId.length;
    if(valid) {
      next();
    } else {
      res.send(400, {error: 'Invalid request body'});
    }
  }

};

module.exports = function(app, logger, db) {

  var middlewares = [MIDDLEWARES.validateRequestBody];

  app.post('/instanceRun', middlewares, function(req, res){
      var now = moment.utc().startOf('day').toDate();
      var instanceRunData = {
        instanceId: req.body.instanceId,
        appVersion: req.body.appVersion,
        date: now
      };

    saveStatsToMongo(instanceRunData, db, function(err) {
      // always respond with 200
      // we don't want clients to be influenced by our server doing bad things
      res.send(200);
    });
  });


  function saveStatsToMongo(stats, db, callback) {
    var startOfDay = moment.utc(stats.date).startOf('day').toDate();
    var endOfDay = moment.utc(stats.date).endOf('day').toDate();

    var queryMatching = {
      instanceId: stats.instanceId,
      runDate: {
          $gte: startOfDay,
          $lt: endOfDay
      }      
    };

    var docToInsert = {
      $setOnInsert: {
        instanceId: stats.instanceId,
        runDate: stats.date,
        appVersion: stats.appVersion
      },
      $inc: { runCount: 1 }
    };

    db.collection('instanceRunStats').update(queryMatching, docToInsert, {upsert: true}, function(err) {
      if(err) {
        console.error('Could not save stats.', stats, err);
        return callback(err);
      }
      callback(null);
    });
  }

};