angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsListService, $state) {

        $scope.switchListView = function() {
            if($scope.listViewMode && $scope.listViewMode == 'all') {
                $scope.loadAllCommits();
                _switchStateToCommitList();
            } else {
                $scope.loadPendingCommits();
                _switchStateToCommitList();
            }
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

        function initCtrl() {
            $scope.listViewMode = 'pending';
            $scope.loadPendingCommits();
        }

        initCtrl();

    });