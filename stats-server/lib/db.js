var MongoClient = require('mongodb');
    Q = require('q');

function initialize(callback) {
  var connect = Q.denodeify(MongoClient.connect);
  var dbInitialized = connect("mongodb://localhost:27017/codebrag_stats");
  dbInitialized.then(ensureIndex);
  return dbInitialized.nodeify(callback);   // return promise, but allow for callback style too
}

function ensureIndex(db) {
  var indexFields = {
    instanceId: 1,
    date: 1
  };
  return Q.ninvoke(db.collection('statistics'), 'ensureIndex', indexFields, {unique:true});
}

module.exports = {
  initialize: initialize
};
