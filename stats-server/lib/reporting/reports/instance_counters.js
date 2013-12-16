var genericLoader = require('./generic_report_loader');

function aggregationSteps(instanceId) {
  return [
    {
      $match: {
        instanceId: instanceId
      }
    },{
      $project: {
        _id: 0,
        date: 1,
        counters: 1
      }
    },{
      $sort: {
        date: 1
      }
    }
  ];
}

function wrapResults(instanceId) {
  return function(results) {
    return {
      instanceId: instanceId,
      stats: results
    };
  };
}

module.exports = {
  load: function countersPerDay(db, instanceId) {
    var steps = aggregationSteps(instanceId);
    return genericLoader.makeMongoAggregation(db, steps).then(wrapResults(instanceId));
  }
};

