var MongoClient = require('mongodb');

var db;

function initialize(callback) {
  MongoClient.connect("mongodb://localhost:27017/codebrag", function(err, _db) {
    if(err) return callback(err);
    db = _db;
    ensureIndex(db, callback);
  });
}

function ensureIndex(db, callback) {
  var index = {
    instanceId: 1,
    date: 1
  };
  db.collection('stats').ensureIndex(index, {unique:true}, function(err) {
    if(err) return callback(err);
    callback(null, db);
  });
}

module.exports = {
  initialize: initialize,
  db: db
};