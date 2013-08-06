angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, commitsListService, $stateParams) {

        $scope.switchListView = function() {
            if($scope.listViewMode && $scope.listViewMode == 'all') {
                $scope.loadAllCommits();
            } else {
                $scope.loadPendingCommits();
            }
        };

        $scope.loadAllCommits = function () {
            if($stateParams.id) {
                $scope.commits = commitsListService.loadSurroundings($stateParams.id);
            } else {
                $scope.commits = commitsListService.loadAllCommits();
            }
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