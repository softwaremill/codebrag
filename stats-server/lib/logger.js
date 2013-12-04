var winston = require('winston');

winston.add(winston.transports.File, {
  filename: __dirname + '/../statistics.log'
});
winston.remove(winston.transports.Console);

module.exports = winston;