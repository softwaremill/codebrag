use codebrag;

db.statistics.find().forEach(function(record) {
  var counters = record.counters;
  var isActive = Object.keys(counters).filter(function(key) {
    return counters[key] > 0;
  }).length > 0;
  record.active = isActive;
  db.statistics.save(record);
});