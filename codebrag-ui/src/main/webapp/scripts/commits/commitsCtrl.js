angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadAllCommits = function() {
            $scope.commits = commitsListService.loadAllCommits();
        };

        $scope.loadPendingCommits = function() {
            $scope.commits = commitsListService.loadCommitsPendingReview();
        };


        $scope.loadPendingCommits();

    });