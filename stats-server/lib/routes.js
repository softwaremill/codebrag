module.exports = function(app, logger) {

  app.post('/', function(req, res){
    logger.log('info', 'Stats', {stats: req.body});
    res.send(200);
  });

  app.get('/', function(req, res){
    res.send('Hello from Codebrag stats server!');
  });

};