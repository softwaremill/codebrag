var genericLoader = require('./generic_report_loader');

var AGGREGATION_STEPS = [
  {
    $group: {
      _id: "$date",
      commitsReviewedCount: { $sum: "$counters.commitsReviewedCount"},
      likesCount: { $sum: "$counters.likesCount"},
      commentsCount: { $sum: "$counters.commentsCount"},
      activeUsersCount: { $sum: "$counters.activeUsersCount"},
      registeredUsersCount: { $sum: "$counters.registeredUsers"}
    }
  }, {
    $project: {
      date: "$_id",
      _id: 0,
      counters: {
        commitsReviewedCount: "$commitsReviewedCount",
        likesCount: "$likesCount",
        commentsCount: "$commentsCount",
        activeUsersCount: "$activeUsersCount",
        registeredUsersCount: "$registeredUsersCount"
      }
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
  load: function countersPerDay(db) {
    return genericLoader.makeMongoAggregation(db, AGGREGATION_STEPS).then(wrapResults);
  }
};

