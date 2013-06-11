angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService, commitLoadFilter, $state, events) {

        $scope.syncCommits = commitsListService.syncCommits;
        $scope.toReviewCount = 0;
        $scope.loadedCommitCount = 0;

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
            _loadCommitsFromPromise(commitsListService.loadAllCommits());
        };

        $scope.loadPendingCommits = function () {
            _loadCommitsFromPromise(commitsListService.loadCommitsPendingReview());
        };

        function _loadCommitsFromPromise(promise) {
            promise.then(function(commits) {
                $scope.commits = commits;
                $scope.loadedCommitCount = commits.length;
            })
        }
        $scope.canLoadMore = function () {
            return !commitLoadFilter.isAll() && $scope.toReviewCount > $scope.loadedCommitCount;
        };

        $scope.loadMoreCommits = function () {
            _loadCommitsFromPromise(commitsListService.loadMoreCommits());
        };

        $scope.loadPendingCommits();

        $scope.$on(events.commitCountChanged, function(event, data) {
            $scope.toReviewCount = data.commitCount;
        });


    });