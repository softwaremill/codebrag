var winston = require('winston');

winston.add(winston.transports.File, {
  filename: __dirname + '/../statistics.log',
  maxsize: 10240
});
winston.remove(winston.transports.Console);

module.exports = winston;