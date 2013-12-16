var genericLoader = require('./generic_report_loader');

var AGGREGATION_STEPS = [
  {
    $group: {
      _id: "$date",
      instancesCount: {
        $sum: 1
      }
    }
  }, {
    $project: {
      date: "$_id",
      instancesCount: 1,
      _id: 0
    }
  },{
    $sort: {
      date: 1
    }
  }
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

