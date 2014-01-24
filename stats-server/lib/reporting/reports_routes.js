var Q = require('q');
var _ = require('lodash');

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

// load all instances stats and active instances stats
// and mix them together to allow displaying on one chart easily
function collectInstancesPerDay(db) {
    var instancesPerDay = instancesPerDayReport.load(db);
    var activePerDay = activeInstancesPerDay.load(db);
    return Q.all([instancesPerDay, activePerDay]).spread(joinAllAndActive);

    function joinAllAndActive(instances, active) {
      var all = [];
      Array.prototype.push.apply(all, instances.stats);
      Array.prototype.push.apply(all, active.stats);
      var allGroupByDate = _.groupBy(all, 'date');
      var merged = _.values(allGroupByDate).map(function(group) {
        return {
          date: group[0].date,
          allCount: getOrDefault(group[0], 'instancesCount', 0),
          activeCount: getOrDefault(group[1], 'instancesCount', 0)
        }

        function getOrDefault(obj, prop, defaultVal) {
          if(obj != null && obj[prop] != null) {
            return obj[prop];
          }
          return defaultVal;
        }
      });

      return {
        stats: merged 
      }
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

