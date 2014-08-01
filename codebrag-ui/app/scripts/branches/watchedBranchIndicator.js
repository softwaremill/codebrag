angular.module('codebrag.branches')

    .directive('watchedBranchIndicator', function() {

        return {
            restrict: 'E',
            replace: true,
            templateUrl: "views/branches/watchedBranchIndicator.html",
            scope: {
                branch: '=',
                toggleWatching: '&'
            }
        }

    });