var genericLoader = require('./generic_report_loader');

var AGGREGATION_STEPS = [
  {
    $match: {
        $or: [
            {"counters.commitsReviewedCount": { $gt: 0 }},
            {"counters.likesCount": { $gt: 0 }},
            {"counters.commentsCount": { $gt: 0 }},
            {"counters.activeUsersCount": { $gt: 0 }},
            {"counters.registeredUsers": { $gt: 0 }}
        ]
    }
  }, {
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
  }, {
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

