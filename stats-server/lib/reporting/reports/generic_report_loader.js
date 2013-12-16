var Q = require('q');

/*
  Wraps Mongo aggregation framework operation and returns promise with result
*/
module.exports = {
  makeMongoAggregation: function(db, aggregationSteps) {
    var statsColl = db.collection('statistics');
    var aggregate = Q.nbind(statsColl.aggregate, statsColl);
    return aggregate(aggregationSteps);
  }
};
