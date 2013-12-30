var stats = angular.module('stats', ['ngRoute']);

stats.config(function($routeProvider) {
  $routeProvider
    .when('/instances-per-day', {
      templateUrl: 'instancesPerDay'
    })
    .when('/counters-per-day', {
      templateUrl: 'countersPerDay'
    })
    .otherwise({
      redirectTo: '/'
    });
});

stats.controller('StatsCtrl', function($scope, statsDataService, $location) {

  $scope.openInstancesPerDayReport = function() {
    statsDataService.instancesPerDayReport().then(function(reportData) {
      $scope.reportData = {};
      $scope.reportData.allInstances= toSeries(transformToChartSeries(reportData.allInstances));
      $scope.reportData.activeInstances = toSeries(transformToChartSeries(reportData.activeInstances));
      $location.path('/instances-per-day');
    });

    function transformToChartSeries(reportData) {
      return reportData.map(function(entry) {
        return {
          x: moment(entry.date).unix(),
          y: entry.instancesCount
        };
      });
    }

    function toSeries(xyData) {
      return [
        {
          color: 'green',
          data: xyData,
          name: 'Instances'
        }
      ];
    }
  };

  $scope.openCountersPerDayReport = function() {
    statsDataService.countersPerDayReport().then(function(reportData) {
      $scope.reportData = toSeries(reportData);
      $location.path('/counters-per-day');
    });

    function toSeries(data) {
      var series = {};
      data.forEach(function(entry) {
        Object.keys(entry.counters).forEach(function(counterName) {
          var singleEntry = {
            x: moment(entry.date).unix(),
            y: entry.counters[counterName]
          };
          if(series[counterName]) {
            series[counterName].push(singleEntry);
          } else {
            series[counterName] = [singleEntry];
          }
        });
      });
      var colors = ['blue', 'green', 'red', 'yellow', 'gray'];
      var series2 = {};
      Object.keys(series).forEach(function(serieName) {
        series2[serieName] = [{
          color: colors.shift(),
          data: series[serieName],
          name: serieName
        }];
      });
      return series2;
    }

  };

});

stats.service('statsDataService', function($http) {

  this.instancesPerDayReport = function() {
    return issueGet('/reports/instances-per-day');
  };

  this.countersPerDayReport = function() {
    return issueGet('/reports/counters-per-day');
  };

  function issueGet(url) {
    return $http.get(url).then(success, err);
  }

  function success(response) {
    return response.data.stats;
  }

  function err(response) {
    console.log('Got error in HTTP response', response);
    throw new Error('Something went wrong');
  }

});


stats.directive('graph', function($window) {

  return {
    restrict: 'E',
    template: '<section><h2 ng-show="title">{{title}}</h2><div class="graph"></div></section>',
    scope: {
      data: '=',
      title: '@'
    },
    link: function(scope, el) {
      var unwatch = scope.$watch('data', function(data) {
        if(angular.isUndefined(data)) return;
        unwatch();
        var graphOptions = {
          element: el.find('div')[0],
          width: 900,
          height: 300,
          renderer: 'bar',
          series: data
        };

        var graph = new Rickshaw.Graph(graphOptions);
        var xAxis = new Rickshaw.Graph.Axis.Time({graph: graph});
        var yAxis = new Rickshaw.Graph.Axis.Y({graph: graph});
        graph.render();
      });
    }
  };
});