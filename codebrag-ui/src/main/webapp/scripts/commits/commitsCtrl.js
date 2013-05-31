angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter, $state) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.switchToAll = function () {
            $scope.loadAllCommits();
            _switchStateToCommitList();
        };

        $scope.switchToPending = function () {
            $scope.loadPendingCommits();
            _switchStateToCommitList();
        };

        function _switchStateToCommitList() {
            $state.transitionTo('commits.list');
        }

        $scope.loadAllCommits = function () {
            $scope.commits = commitsListService.loadAllCommits();
        };

        $scope.loadPendingCommits = function () {
            $scope.commits = commitsListService.loadCommitsPendingReview();
        };

        $scope.canLoadMore = function () {
            return !commitLoadFilter.isAll();
        };

        $scope.loadMoreCommits = function () {
            $scope.commits = commitsListService.loadMoreCommits();
        };

        $scope.loadPendingCommits();

    });