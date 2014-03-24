angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsService, $stateParams, $state, currentCommit, events, pageTourService) {

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

        $scope.openCommitDetails = function(sha) {
            $state.transitionTo('commits.details', {sha: sha});
        };

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length === 0);
            var noMoreCommitsOnServer = !commitsService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };

        $scope.hasCommitsAvailable = function() {
            return $scope.commits && $scope.commits.length > 0;
        };

        $scope.pageTourForCommitsVisible = function() {
            return pageTourService.stepActive('commits') || pageTourService.stepActive('invites');
        };

        function loadAllCommits() {
            commitsService.setAllMode();
            commitsService.loadCommits($stateParams.sha).then(function(commits) {
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
            $scope.switchListView();
            if(angular.isUndefined($scope.listViewMode)) {
                $scope.listViewMode = 'pending';
            }
        }

        initCtrl();

    });