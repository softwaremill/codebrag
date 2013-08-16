angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsListService, $stateParams, $state) {

        $scope.switchListView = function() {
            if($scope.listViewMode === 'all') {
                loadAllCommits();
            } else {
                loadPendingCommits();
            }
        };

        $scope.hasNextCommits = function() {
            return commitsListService.hasNextCommits();
        };

        $scope.hasPreviousCommits = function() {
            return commitsListService.hasPreviousCommits();
        };

        $scope.loadNextCommits = function() {
            commitsListService.loadNextCommits();
        };

        $scope.loadPreviousCommits = function() {
            commitsListService.loadPreviousCommits();
        };

        $scope.openCommitDetails = function(commitId) {
            $state.transitionTo('commits.details', {id: commitId});
        };

        $scope.markAsReviewed = function(commitId) {
            commitsListService.makeReviewedAndGetNext(commitId).then(function(next) {
                if (next) {
                    $state.transitionTo('commits.details', {id: next.id});
                } else {
                    $state.transitionTo('commits.list');
                }
            });
        };

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length == 0);
            var noMoreCommitsOnServer = !commitsListService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };



        function loadAllCommits() {
            if($stateParams.id) {
                $scope.commits = commitsListService.loadCommitsInContext($stateParams.id);
            } else {
                $scope.commits = commitsListService.loadCommitsInContext();
            }
        }

        function loadPendingCommits() {
            commitsListService.loadCommitsToReview().then(function(commits) {
                $scope.commits = commits;
            })
        }

        $scope.initCtrl = function() {
            $scope.listViewMode = 'pending';
            loadPendingCommits();
        };

        $scope.initCtrl();

    });