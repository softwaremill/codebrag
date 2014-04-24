var genericLoader = require('./generic_report_loader');

var AGGREGATION_STEPS = [
    { $project: { instanceId: 1, date: 1, active: 1, _id: 0, appVersion: 1} },
    { $group: { _id: {id: "$instanceId", appVersion: "$appVersion"}, activityDates: { $push: { date: "$date", active: "$active" } } } },
    { $project: {  instanceId: "$_id.id",  appVersion: "$_id.appVersion", _id: 0, activityDates: 1 } }
];

function wrapResults(results) {
    return {
        stats: results
    };
}

// sort by date of first run
function sortByAppearDate(results) {
    return results.sort(function(a,b) {
        return b.activityDates[0].date.getTime() - a.activityDates[0].date.getTime();
    });
}

module.exports = {
    load: function instancesPerDay(db) {
        return genericLoader.makeMongoAggregation(db, AGGREGATION_STEPS).then(sortByAppearDate).then(wrapResults);
    }
};
