angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadAllCommits = function() {
            $scope.commits = commitsListService.loadAllCommits();
        };

        $scope.loadPendingCommits = function() {
            $scope.commits = commitsListService.loadCommitsPendingReview();
        };

        $scope.canLoadMore = function() {
            return !commitLoadFilter.isAll();
        };

        $scope.loadMoreCommits = function() {
            $scope.commits = commitsListService.loadMoreCommits();
        };

        $scope.loadPendingCommits();

    });