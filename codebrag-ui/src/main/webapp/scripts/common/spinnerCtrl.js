angular.module('codebrag.common')

    .controller('SpinnerCtrl', function($scope, httpRequestTracker) {

        $scope.hasPendingRequests = function () {
            return httpRequestTracker.hasPendingRequests();
        };

    });