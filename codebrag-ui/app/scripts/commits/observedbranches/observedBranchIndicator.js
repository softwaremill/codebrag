angular.module('codebrag.commits.observedbranches')

    .directive('observedBranchIndicator', function() {

        return {
            restrict: 'E',
            template: '<span style="display: inline-block; position: relative; left: -30px" ng-click="toggleObserved(repoName, branchName)">' +
                '<i class="icon-bell" ng-show="!observed"></i>' +
                '<i class="icon-bell-alt" ng-show="observed"></i>' +
                '</span>',
            scope: {
                repoName: '=',
                branchName: '='
            },
            controller: 'ObservedBranchCtrl'
        }

    })

    .controller('ObservedBranchCtrl', function($scope) {

        $scope.observed = false;

        $scope.toggleObserved = function(repoName, branchName) {
            console.log('will toggle observed branch:', repoName, branchName);
            $scope.observed = !$scope.observed;
        }

    });