var genericLoader = require('./generic_report_loader');

var AGGREGATION_STEPS = [
  { $project: { instanceId: 1, date: 1, active: 1, _id: 0 } },
  { $group: { _id: "$instanceId",  activityDates: { $push: { date: "$date", active: "$active" } } } },
  { $project: {  instanceId: "$_id",  _id: 0, activityDates: 1 } }
];

function wrapResults(results) {
  return {
    stats: results
  };
}

module.exports = {
  load: function instancesPerDay(db) {
    return genericLoader.makeMongoAggregation(db, AGGREGATION_STEPS).then(wrapResults);
  }
};