var Q = require('q');

/*
  Wraps Mongo aggregation framework operation and returns promise with result
*/
module.exports = {
  makeMongoAggregation: function(db, aggregationSteps, collection) {
    var collectionName = collection || 'statistics';
    var statsColl = db.collection(collectionName);
    var aggregate = Q.nbind(statsColl.aggregate, statsColl);
    return aggregate(aggregationSteps);
  }
};
