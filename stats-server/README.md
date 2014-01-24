##Simple stats server for Codebrag

### Beware - it's written in JS (nodejs, angular) - you have been warned :)

![Beware](http://ecx.images-amazon.com/images/I/41Z6n%2BjZcKL._SL500_AA300_.jpg)

### You can access production version of this stats server at [http://stats.codebrag.com:8080](http://stats.codebrag.com:8080) (requires authentication: codebrag/\_showmestats\_)

#### Requirements
- nodejs 0.10+ (with bower installed)
- mongodb 2.4.x

#### Details

It has two parts:

- one for collecting statistics 
- one for reporting purposes


#### Collecting stats
There is a nodejs server running on port `6666` that you can `POST` to and if your request data looks reasonable, it'll be saved in Mongo `statistics` collection in `codebrag` database. 

By default stats on Codebrags are sent at 3am and only one stat entry for given instance id will be saved for given day. In other words there is an unique index on (instanceId, date)

#### Reporting
Reporting is run as another server and it's code is in `lib/reporting/` and `public/` for frontend stuff. It basically uses Mongo Aggregation framework to massage stats data to be meaningful. Then it just hands them over to the client as JSON.

Then the frontend magic happens (using Angular and Morris) supported by Bower for packages management.

#### Development

** Before throwing WFTs at me type and run: `npm install` and `bower install` to install all dependencies required for both backend and frontend ** 

Installing new backend dependencies - issue `npm install --save [package]`.

Installing new frontend dependency - issue `bower install --save [package]` and package will land in `public/vendor`. You have to include files in `index.html` by hand.

Any change in backend part requires restarting server. If you want, you can use `nodemon` to auto-restart it on every change - maybe will add it in future.

#### Running

To continuously run those servers there is "forever" tool used and running scripts are provided. You can separately run collecting server and reporting server as well as change status of those processes using `status.sh`.

Basically what "forever" does is it restarts server if it crashes (any unhandled exception etc).