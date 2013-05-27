angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadAllCommits = function() {
            console.log('load all');
            $scope.commits = commitsListService.loadAllCommits();
            console.log('load all done');
        };

        $scope.loadPendingCommits = function() {
            console.log('load pending');
            $scope.commits = commitsListService.loadCommitsPendingReview();
            console.log('load pending done');
        };


        $scope.loadPendingCommits();

    });