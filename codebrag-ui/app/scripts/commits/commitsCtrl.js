angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsService, $stateParams, $state, currentCommit, events) {

        $scope.$on(events.reloadCommitsList, function() {
            initCtrl();
        });

        $scope.switchListView = function() {
            $scope.listViewMode === 'all' ? loadAllCommits() : loadPendingCommits();
        };

        $scope.hasNextCommits = function() {
            return commitsService.hasNextCommits();
        };

        $scope.hasPreviousCommits = function() {
            return commitsService.hasPreviousCommits();
        };

        $scope.loadNextCommits = function() {
            commitsService.loadNextCommits();
        };

        $scope.loadPreviousCommits = function() {
            commitsService.loadPreviousCommits();
        };

        $scope.openCommitDetails = function(commitId) {
            $state.transitionTo('commits.details', {id: commitId});
        };

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length === 0);
            var noMoreCommitsOnServer = !commitsService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };



        function loadAllCommits() {
            commitsService.setAllMode();
            commitsService.loadCommits($stateParams.id).then(function(commits) {
                $scope.commits = commits;
            })
        }

        function loadPendingCommits() {
            commitsService.setToReviewMode();
            commitsService.loadCommits().then(function(commits) {
                $scope.commits = commits;
            });
        }

        function initCtrl() {
            currentCommit.empty();
            $scope.listViewMode = 'pending';
            loadPendingCommits();
        }

        initCtrl();

    });