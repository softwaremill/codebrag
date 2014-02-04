var _ = require('lodash');
var moment = require('moment');

var MIDDLEWARES = {

  validateRequestBody: function(req, res, next) {
    var json = req.body;
    var dateRegexp = /^\d{2}\/\d{2}\/\d{4}$/;
    var valid = json.instanceId && json.instanceId.length && dateRegexp.test(json.date) && _.isPlainObject(json.counters);
    if(valid) {
      next();
    } else {
      res.send(400, {error: 'Invalid request body'});
    }
  },

  parseStatsDate: function(req, res, next) {
    var stats = _.clone(req.body, true);
    stats.date = moment.utc(stats.date, 'DD/MM/YYYY').toDate();
    req.stats = stats;
    next();
  },

  flagIfInstanceActive: function(req, res, next) {
    var isActive = Object.keys(req.stats.counters).filter(function(key) {
      return req.stats.counters[key] > 0;
    }).length > 0;
    req.stats.active = isActive;
    next();
  }

};

module.exports = function(app, logger, db) {

  var middlewares = [MIDDLEWARES.validateRequestBody, MIDDLEWARES.parseStatsDate, MIDDLEWARES.flagIfInstanceActive];

  app.post('/', middlewares, function(req, res){
    saveStatsToMongo(req.stats, db, function(err) {
      // always respond with 200
      // we don't want clients to be influenced by our server doing bad things
      res.send(200);
    });
  });

  app.get('/', function(req, res){
    res.send('Hello from Codebrag stats server!');
  });

  function saveStatsToMongo(stats, db, callback) {
    db.collection('statistics').insert(stats, function(err) {
      if(err) {
        logger.log('info', 'Stats', stats);
        console.error('Could not save stats. They were logged to statistics.log instead', stats, err);
        return callback(err);
      }
      callback(null);
    });
  }

};