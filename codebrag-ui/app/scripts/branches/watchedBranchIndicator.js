angular.module('codebrag.branches')

    .directive('watchedBranchIndicator', function() {

        return {
            restrict: 'E',
            replace: true,
            template: '<span style="display: inline-block; position: relative; left: -30px" ng-click="toggleWatching(watching)" isolate-click>' +
                '<i class="icon-bell" ng-show="!branch.watching"></i>' +
                '<i class="icon-bell-alt" ng-show="branch.watching"></i>' +
                '</span>',
            scope: {
                branch: '=',
                toggleWatching: '&'
            }
        }

    });