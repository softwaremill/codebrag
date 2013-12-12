var fs = require('fs');
var byline = require('byline');
var _ = require('lodash');
var moment = require('moment');
var MongoClient = require('mongodb').MongoClient;


var statsLogFileName = process.argv[2];
if(!statsLogFileName) {
  console.log('Pass log file name to process as an argument');
  process.exit(1);
}

initMongo(processLogFile);

function processLogFile(db) {
  var stream = byline(fs.createReadStream(statsLogFileName, { encoding: 'utf8' }));
  var entriesMap = {};
  stream.on('data', function(line) {
    addLineAsJson(line, entriesMap);
  });
  stream.on('end', function() {
    var entries = makeUnique(entriesMap);
    store(entries, db);
  });
}

function addLineAsJson(line, entriesMap) {
  var asJson = JSON.parse(line).stats;
  asJson.date = moment.utc(asJson.date, 'DD/MM/YYYY').toDate();
  if(entriesMap[asJson.date]) {
    entriesMap[asJson.date].push(asJson);
  } else {
    entriesMap[asJson.date] = [asJson];
  }
}

function makeUnique(entriesMap) {
  var entries = [];
  for(var date in entriesMap) {
    [].push.apply(entries, _.unique(entriesMap[date], 'instanceId'));
  }
  return entries;
}

function store(entries, db) {
  var left = entries.length;
  entries.forEach(function(stat) {
    db.collection('stats').insert(stat, function(err, result) {
      left--;
      if(err) throw err;
      if(left === 0) {
        db.close();
      }
    });
  });
  console.log('Stored entries:', entries.length);
}

function initMongo(callback) {
  MongoClient.connect("mongodb://localhost:27017/stats", function(err, db) {
    if(err) throw err;
    db.collection('stats').ensureIndex({instanceId:1, date: 1}, {unique:true}, function(err, indexName) {
      if(err) throw err;
      callback(db);
    });
  });
}