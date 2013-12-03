var fs = require('fs');

var certDir = __dirname + '/../cert/';

module.exports.credentials = {
  key: fs.readFileSync(certDir + 'localhost.key').toString(),
  cert: fs.readFileSync(certDir + 'localhost.cert').toString()
};
