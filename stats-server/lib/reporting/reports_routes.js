var instancesPerDayReport = require('./reports/instances_per_day'),
    countersPerDayReport = require('./reports/counters_per_day'),
    instanceCounters = require('./reports/instance_counters');

function sendReportContent(res) {
  return function(report) {
    return res.json(200, report);
  };
}

function sendError(res) {
  return function(err) {
    console.error('Error while generating report', err);
    return res.json(500);
  };
}

module.exports = function(app, db) {

  app.get('/reports/instances-per-day', function(req, res){
    instancesPerDayReport.load(db).done(sendReportContent(res), sendError(res));
  });

  app.get('/reports/counters-per-day', function(req, res){
    countersPerDayReport.load(db).done(sendReportContent(res), sendError(res));
  });

  app.get('/reports/instance-counters/:instanceId', function(req, res){
    var instanceId = req.params.instanceId;
    instanceCounters.load(db, instanceId).done(sendReportContent(res), sendError(res));
  });

};

