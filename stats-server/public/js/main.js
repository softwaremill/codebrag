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
      $scope.reportData = transformToChartSeries(reportData);
      $location.path('/instances-per-day');
    });

    function transformToChartSeries(reportData) {
      return reportData.map(function(entry) {
        return {
          x: moment(entry.date).format('DDMMM'),
          y1: entry.allCount,
          y2: entry.activeCount
        };
      });
    }
  };

  $scope.openCountersPerDayReport = function() {
    statsDataService.countersPerDayReport().then(function(reportData) {
      $scope.reportData = toSeries(reportData);
      console.log($scope.reportData);
      $location.path('/counters-per-day');
    });

    function toSeries(data) {
      return data.map(function(srcEntry) {
        var entry = {}
        angular.copy(srcEntry.counters, entry); // copy entire counters obj to entry
        entry.date = moment(srcEntry.date).format('DDMMM'); // add formatted date to entry
        return entry;
      });
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

stats.directive('morrisGraph', function($window) {

  return {
    restrict: 'E',
    template: '<section style="margin-top: 4em; color: #0ad; display: block; text-align: center"><h2 ng-show="title">{{title}}</h2><div id="{{id}}" style="width: 90%; margin: 0 auto"></div></section>',
    scope: {
      data: '=',
      title: '@',
      xkey: '=',
      ykeys: '=',
      ylabels: '='
    },
    link: function(scope, el) {
      scope.id = String(Math.round(Math.random() * 1000));
      var unwatch = scope.$watch('data', function(data) {
        if(angular.isUndefined(data)) return;
        unwatch();      
        Morris.Bar({
          element: scope.id,
          hideHover: 'auto',
          data: data,
          xkey: scope.xkey,
          ykeys: scope.ykeys,
          labels: scope.ylabels,
          barColors: ['#f90', '#0ad']
        });
      });
    }
  };
});

