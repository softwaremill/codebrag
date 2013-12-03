var fs = require('fs');

var certDir = __dirname + '/../cert/';

module.exports.credentials = {
  key: fs.readFileSync(certDir + 'stats.codebrag.com.key').toString(),
  cert: fs.readFileSync(certDir + 'stats.codebrag.com.cert').toString()
};
