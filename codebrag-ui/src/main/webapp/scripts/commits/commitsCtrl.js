angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsListService, $stateParams, $state, currentCommit) {

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

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length === 0);
            var noMoreCommitsOnServer = !commitsListService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };



        function loadAllCommits() {
            if($stateParams.id) {
                $scope.commits = commitsListService.loadCommitsInContext($stateParams.id);
            } else {
                $scope.commits = commitsListService.loadNewestCommits();
            }
        }

        function loadPendingCommits() {
            commitsListService.loadCommitsToReview().then(function(commits) {
                $scope.commits = commits;
            });
        }

        $scope.initCtrl = function() {
            currentCommit.empty();
            $scope.listViewMode = 'pending';
            loadPendingCommits();
        };

        $scope.initCtrl();

    });