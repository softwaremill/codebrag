var Q = require('q');

var instancesPerDayReport = require('./reports/instances_per_day'),
    countersPerDayReport = require('./reports/counters_per_day'),
    instanceCounters = require('./reports/instance_counters'),
    activeInstancesPerDay = require('./reports/active_instances_per_day');

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

function collectInstancesPerDay(db) {
    var instancesPerDay = instancesPerDayReport.load(db);
    var activePerDay = activeInstancesPerDay.load(db);
    return Q.all([instancesPerDay, activePerDay]).spread(joinAllAndActive);

    function joinAllAndActive(instances, active) {
      return {
        stats: {
          allInstances: instances.stats,
          activeInstances: active.stats
        }
      };
    }
}

module.exports = function(app, db) {

  app.get('/reports/instances-per-day', function(req, res){
      collectInstancesPerDay(db).done(sendReportContent(res), sendError(res));
  });

  app.get('/reports/counters-per-day', function(req, res){
    countersPerDayReport.load(db).done(sendReportContent(res), sendError(res));
  });

  app.get('/reports/instance-counters/:instanceId', function(req, res){
    var instanceId = req.params.instanceId;
    instanceCounters.load(db, instanceId).done(sendReportContent(res), sendError(res));
  });

};

