angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, currentCommit, commitsService, $stateParams, $state, events, pageTourService, currentRepoContext) {

        currentRepoContext.ready().then(loadCommits);

        $scope.$on(events.branches.branchChanged, loadCommits);
        $scope.$on(events.commitsListFilterChanged, loadCommits);
        $scope.$on(events.profile.emailAliasesChanged, loadCommits);

        $scope.hasNextCommits = function() {
            return commitsService.hasNextCommits();                                                                      9
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
            $state.transitionTo('commits.details', {sha: sha, repo: currentRepoContext.repo});
        };

        $scope.allCommitsReviewed = function() {
            var emptyList = ($scope.commits && $scope.commits.length === 0);
            var noMoreCommitsOnServer = !commitsService.hasNextCommits();
            return emptyList && noMoreCommitsOnServer;
        };

        $scope.pageTourForCommitsVisible = function() {
            return pageTourService.stepActive('commits') || pageTourService.stepActive('invites');
        };

        function loadCommits() {
            var currentContextSha = $stateParams.sha;
            commitsService.loadCommits(currentContextSha).then(function(commits) {
                $scope.commits = commits;
            });
        }

    });