angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $rootScope, currentCommit, commitsService, $stateParams, $state, events, pageTourService, branchesService) {

        branchesService.ready().then(loadCommits);

        $scope.$on(events.commitsListFilterChanged, function(event, mode) {
            loadCommits(mode);
        });

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
            if(currentCommit.hasSha(sha)) return;
            currentCommit.empty();
            $state.transitionTo('commits.details', {sha: sha});
        };

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length === 0);
            var noMoreCommitsOnServer = !commitsService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };

        $scope.pageTourForCommitsVisible = function() {
            return pageTourService.stepActive('commits') || pageTourService.stepActive('invites');
        };

        function loadCommits(mode) {
            mode === 'all' ? loadAllCommits() : loadPendingCommits();
        }

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

    });