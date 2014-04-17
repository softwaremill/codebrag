angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $rootScope, currentCommit, commitsService, $stateParams, $state, events, pageTourService, countersService) {

        $scope.$on(events.branches.branchChanged, initCtrl);
        $scope.$on(events.commitsTabOpened, initCtrl);

        $scope.switchListView = function(newMode) {
            if(newMode && newMode === $scope.listViewMode) return;
            if(newMode) {
                $scope.listViewMode = newMode;
            }
            if(angular.isUndefined($scope.listViewMode)) {
                $scope.listViewMode = 'pending';
            }
            $scope.listViewMode === 'all' ? loadAllCommits() : loadPendingCommits();
            $rootScope.$broadcast(events.commitsListFilterChanged);
        };

        $scope.displaySelectedMode = function() {
            return $scope.listViewMode === 'all' ? 'all' : $scope.toReviewLabel();
        };

        $scope.toReviewLabel = function() {
            return 'to review (' + countersService.commitsCounter.currentCount() + ')';
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
            currentCommit.empty();
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
        }

        initCtrl();

    });