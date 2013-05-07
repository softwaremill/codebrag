angular.module('codebrag.commits')

    .controller('CommitsCtrl', function ($scope, $http, commitsListService) {

        $scope.AVAILABLE_MODES = {
            ALL: {
                name: "All",
                value: "all"
            },
            PENDING_REVIEW: {
                name: "Pending review",
                value: "pending"
            }
        };

        $scope.loadMode = $scope.AVAILABLE_MODES.PENDING_REVIEW;
        $scope.syncCommits = commitsListService.syncCommits;

        $scope.loadCommits = function() {
            commitsListService.loadCommitsFromServer($scope.loadMode.value);
            $scope.commits = commitsListService.allCommits();
        };

        $scope.loadCommits();
    });