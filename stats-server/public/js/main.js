var stats = angular.module('stats', ['ngRoute']);

stats.config(function($routeProvider) {
  $routeProvider
    .when('/instances-per-day', {
      templateUrl: 'instancesPerDay'
    })
    .when('/counters-per-day', {
      templateUrl: 'countersPerDay'
    })
    .when('/instance-starts-per-day', {
      templateUrl: 'instanceStartsPerDay'
    })
    .when('/instance-life', {
      templateUrl: 'instanceLife'
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

  $scope.openInstanceStartsPerDayReport = function() {
    statsDataService.instanceStartsPerDayReport().then(function(reportData) {
      $scope.reportData = transformToChartSeries(reportData);
      $location.path('/instance-starts-per-day');
    });

    function transformToChartSeries(reportData) {
      return reportData.map(function(entry) {
        return {
          x: moment(entry.date).format('DDMMM'),
          y1: entry.uniqueInstances.length,
          y2: entry.allRunsCount
        };
      });
    }
  };

  $scope.openCountersPerDayReport = function() {
    statsDataService.countersPerDayReport().then(function(reportData) {
      $scope.reportData = toSeries(reportData);
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

  $scope.openInstanceLifeReport = function() {
    statsDataService.instanceLifeReport().then(function(reportData) {      
      $scope.reportData = toSeries(reportData);
      $location.path('/instance-life');
    });

    function toSeries(data) {
      return data.map(function(entry) {
	    var appVersion = entry.appVersion || '';
        var values = entry.activityDates.map(function(entry) {
          return {
            to: "/Date(" + moment.utc(entry.date).startOf('day').valueOf() + ")/",
            from: "/Date(" + moment.utc(entry.date).endOf('day').valueOf() + ")/",
            customClass: [(entry.active === true ? 'active' : 'inactive'), 'v' + appVersion.replace(/\./g, '')].join(" ")
          }
        });

        return {
          name: entry.instanceId,
          values: values
        }
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

  this.instanceStartsPerDayReport = function() {
    return issueGet('/reports/instance-starts-per-day').then(cleanupDuplicatedInstances);

    function cleanupDuplicatedInstances(collection) {
      return collection.map(function(el) {
        el.uniqueInstances = removeInstancesIfSeen(el.uniqueInstances);
        return el;
      });

      function removeInstancesIfSeen(instances) {
        removeInstancesIfSeen._seen = removeInstancesIfSeen._seen || new Set();
        return instances.filter(function(instance) {
          if(removeInstancesIfSeen._seen.has(instance)) {
            return false;
          }
          removeInstancesIfSeen._seen.add(instance);
          return true;
        });
      }
    }

  };

  this.instanceLifeReport = function() {
    return issueGet('/reports/instance-life');
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
    transclude: true,
    restrict: 'E',
    templateUrl: 'graphArea',
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

stats.directive('ganttGraph', function() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'graphArea',
    transclude: true,
    scope: {
      title: '@',
      data: '=',
      extraClass: '@'
    },
    link: function(scope, el, attrs) {
      scope.id = String(Math.round(Math.random() * 1000));      
      var unwatch = scope.$watch('data', function(data) {
        if(angular.isUndefined(data)) return;
        unwatch();      
        var $rootGanttEl = $("#" + scope.id);
        $rootGanttEl.gantt({
          scale: "days",
          minScale: "days",
          maxScale: "weeks",
          itemsPerPage: 100,
          navigate: "scroll",
          source: data
        });        
      });
    }
  }
});

