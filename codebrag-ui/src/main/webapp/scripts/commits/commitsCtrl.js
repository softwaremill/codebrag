angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter, $state) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadAllCommits = function() {
            $scope.commits = commitsListService.loadAllCommits();
            $state.transitionTo('commits.list');
        };

        $scope.loadPendingCommits = function() {
            $scope.commits = commitsListService.loadCommitsPendingReview();
            $state.transitionTo('commits.list');
        };

        $scope.canLoadMore = function() {
            return !commitLoadFilter.isAll();
        };

        $scope.loadMoreCommits = function() {
            $scope.commits = commitsListService.loadMoreCommits();
        };

        $scope.loadPendingCommits();

    });