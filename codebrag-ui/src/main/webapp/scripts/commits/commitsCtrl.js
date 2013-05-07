angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter) {

        commitLoadFilter.current = commitLoadFilter.modes.pending;

        $scope.filter = commitLoadFilter;
        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadCommits = function() {
            commitsListService.loadCommitsFromServer($scope.filter.current);
            $scope.commits = commitsListService.allCommits();
        };

        $scope.loadCommits();
    });